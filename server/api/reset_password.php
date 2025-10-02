<?php
// reset_password.php
// Accepts {reset_token, new_password} OR {ph906, new_password} OR {email, new_password}
// Updates user_credentials.custom_password with a secure hash.

ini_set('display_errors', 0);
error_reporting(E_ALL);
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');

require_once __DIR__ . '/config.php';
require_once __DIR__ . '/helpers.php';

function readInputArray() {
    $post = $_POST ?? [];
    if (!empty($post)) return $post;
    $raw = file_get_contents('php://input');
    if ($raw) {
        $json = json_decode($raw, true);
        if (is_array($json)) return $json;
        parse_str($raw, $form);
        if (is_array($form) && !empty($form)) return $form;
    }
    return [];
}

try {
    $d = readInputArray();
    $token = $d['reset_token'] ?? '';
    $email = $d['email'] ?? '';
    $ph906Raw = $d['ph906'] ?? '';
    $new = $d['new_password'] ?? '';

    if (empty($new)) {
        http_response_code(400);
        echo json_encode(['status'=>'error','message'=>'new_password required']);
        exit;
    }
    if (strlen($new) < 4) {
        http_response_code(400);
        echo json_encode(['status'=>'error','message'=>'new_password too short']);
        exit;
    }

    $ph906 = null;

    if (!empty($token)) {
        // Validate token from reset_tokens
        $pdo->exec("CREATE TABLE IF NOT EXISTS reset_tokens (
            id INT AUTO_INCREMENT PRIMARY KEY,
            ph906 VARCHAR(255) NOT NULL,
            token VARCHAR(255) NOT NULL UNIQUE,
            expires_at DATETIME NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            INDEX (ph906)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        $stmt = $pdo->prepare("SELECT ph906, expires_at FROM reset_tokens WHERE token=? LIMIT 1");
        $stmt->execute([$token]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        if (!$row) {
            http_response_code(400);
            echo json_encode(['status'=>'error','message'=>'Invalid reset token']);
            exit;
        }
        if (strtotime($row['expires_at']) < time()) {
            // Expired: remove it to clean up and error out
            $pdo->prepare("DELETE FROM reset_tokens WHERE token=?")->execute([$token]);
            http_response_code(400);
            echo json_encode(['status'=>'error','message'=>'Reset token expired']);
            exit;
        }
        $ph906 = $row['ph906'];
    } elseif (!empty($ph906Raw)) {
        // Allow direct reset via ph906 (e.g., after OTP verify) - ensure the account exists
        $stmt = $pdo->prepare("SELECT ph906 FROM masterlist WHERE ph906=? LIMIT 1");
        $stmt->execute([$ph906Raw]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        if (!$row) {
            http_response_code(404);
            echo json_encode(['status'=>'error','message'=>'Account not found for ph906']);
            exit;
        }
        $ph906 = $row['ph906'];
    } elseif (!empty($email)) {
        // Optional email-based reset (only if you have email column in masterlist)
        try {
            $stmt = $pdo->prepare("SELECT ph906 FROM masterlist WHERE email=? LIMIT 1");
            $stmt->execute([$email]);
            $row = $stmt->fetch(PDO::FETCH_ASSOC);
            if (!$row) {
                http_response_code(404);
                echo json_encode(['status'=>'error','message'=>'Account not found for email']);
                exit;
            }
            $ph906 = $row['ph906'];
        } catch (Throwable $e) {
            http_response_code(400);
            echo json_encode(['status'=>'error','message'=>'Email-based reset not supported on server']);
            exit;
        }
    } else {
        http_response_code(400);
        echo json_encode(['status'=>'error','message'=>'Provide reset_token or ph906 or email']);
        exit;
    }

    // Ensure target account exists in masterlist
    $stmt = $pdo->prepare("SELECT ph906 FROM masterlist WHERE ph906=? LIMIT 1");
    $stmt->execute([$ph906]);
    if (!$stmt->fetch()) {
        http_response_code(404);
        echo json_encode(['status'=>'error','message'=>'Student not found']);
        exit;
    }

    // Ensure user_credentials exists
    try {
        $pdo->exec("CREATE TABLE IF NOT EXISTS user_credentials (
            ph906 VARCHAR(255) PRIMARY KEY,
            custom_password VARCHAR(255),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    } catch (Throwable $ignored) {}

    // Hash and upsert password
    $hashed = password_hash($new, PASSWORD_DEFAULT);
    $stmt = $pdo->prepare("INSERT INTO user_credentials (ph906, custom_password) VALUES (?, ?)
        ON DUPLICATE KEY UPDATE custom_password = VALUES(custom_password), updated_at = NOW()");
    $stmt->execute([$ph906, $hashed]);

    // Consume token if present
    if (!empty($token)) {
        $pdo->prepare("DELETE FROM reset_tokens WHERE token=?")->execute([$token]);
    }

    echo json_encode(['status'=>'success','message'=>'Password reset successful']);
} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode(['status'=>'error','message'=>'Reset failed','debug'=>$e->getMessage()]);
}

