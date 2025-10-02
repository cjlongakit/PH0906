<?php
// changepassword.php — direct endpoint wrapper that uses your existing logic
ini_set('display_errors', 0);
error_reporting(E_ALL);
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');

require_once __DIR__ . '/config.php';
require_once __DIR__ . '/helpers.php';
require_once __DIR__ . '/middleware.php';

// The original function body you shared
function handleChangePassword($pdo) {
    $data = inputJSON();
    $current = $data['current_password'] ?? '';
    $new = $data['new_password'] ?? '';
    $ph906 = $GLOBALS['current_student_ph906'] ?? null;

    if (!$ph906) {
        http_response_code(401);
        echo json_encode(["status"=>"error","message"=>"Unauthorized"]);
        return;
    }

    if (empty($current) || empty($new)) {
        http_response_code(400);
        echo json_encode(["status"=>"error","message"=>"current_password and new_password required"]);
        return;
    }

    if (strlen($new) < 4) {
        http_response_code(400);
        echo json_encode(["status"=>"error","message"=>"new_password too short"]);
        return;
    }

    $stmt = $pdo->prepare("SELECT birthday FROM masterlist WHERE ph906=? LIMIT 1");
    $stmt->execute([$ph906]);
    $student = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$student) {
        http_response_code(404);
        echo json_encode(["status"=>"error","message"=>"Student not found"]);
        return;
    }

    $dbBirthday = $student['birthday'];

    $stmt = $pdo->prepare("SELECT custom_password FROM user_credentials WHERE ph906=? LIMIT 1");
    $stmt->execute([$ph906]);
    $cred = $stmt->fetch(PDO::FETCH_ASSOC);

    $currentOk = false;
    if ($cred && !empty($cred['custom_password'])) {
        if (password_verify($current, $cred['custom_password'])) $currentOk = true;
    }

    if (!$currentOk) {
        $norm = normalizeDateToYmd($current);
        if ($norm === $dbBirthday || $current === $dbBirthday) $currentOk = true;
    }

    if (!$currentOk) {
        http_response_code(400);
        echo json_encode(["status"=>"error","message"=>"Current password incorrect"]);
        return;
    }

    // Ensure user_credentials table exists
    try {
        $pdo->exec("CREATE TABLE IF NOT EXISTS user_credentials (
            ph906 VARCHAR(255) PRIMARY KEY,
            custom_password VARCHAR(255),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    } catch (Throwable $ignored) {}

    $hashed = password_hash($new, PASSWORD_DEFAULT);
    $stmt = $pdo->prepare("INSERT INTO user_credentials (ph906, custom_password) VALUES (?, ?) ON DUPLICATE KEY UPDATE custom_password = VALUES(custom_password), updated_at = NOW()");
    $stmt->execute([$ph906, $hashed]);

    echo json_encode(["status"=>"success", "message"=>"Password changed successfully"]);
}

// Auth as student and execute
try {
    checkStudentAuth($STUDENT_API_KEY);
    handleChangePassword($pdo);
} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode(["status"=>"error","message"=>"Change password failed","debug"=>$e->getMessage()]);
}

