<?php
declare(strict_types=1);

ini_set('display_errors', '0');
error_reporting(E_ALL);
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

function readInputArray(): array {
    if (!empty($_POST)) return $_POST;
    $raw = file_get_contents('php://input');
    if ($raw) {
        $json = json_decode($raw, true);
        if (is_array($json)) return $json;
        parse_str($raw, $form);
        if (is_array($form) && !empty($form)) return $form;
    }
    return [];
}

function normalizeDateToYmd($s) {
    if ($s === null || $s === "") return null;
    // Already in YYYY-MM-DD format
    if (preg_match('/^\d{4}-\d{2}-\d{2}/', $s)) {
        return substr($s, 0, 10);
    }
    // Try parsing with strtotime
    $ts = strtotime($s);
    if ($ts !== false) return date("Y-m-d", $ts);
    // Try replacing / with -
    $s2 = str_replace("/", "-", $s);
    $ts2 = strtotime($s2);
    if ($ts2 !== false) return date("Y-m-d", $ts2);
    return $s;
}

try {
    // Use global $pdo from api.php or load it
    global $pdo;
    if (!isset($pdo)) {
        require_once __DIR__ . '/config.php';
    }

    error_log("=== Request Password Reset Started ===");

    $d = readInputArray();
    // Accept both 'ph906' and 'username' fields
    $ph906Raw = trim((string)($d['ph906'] ?? ($d['username'] ?? '')));
    $birthday = trim((string)($d['birthday'] ?? ''));

    error_log("Input - username: $ph906Raw, birthday: $birthday");

    if ($ph906Raw === '') {
        http_response_code(400);
        echo json_encode(['status' => 'error', 'message' => 'Username is required']);
        exit;
    }

    if ($birthday === '') {
        http_response_code(400);
        echo json_encode(['status' => 'error', 'message' => 'Birthday is required']);
        exit;
    }

    // Extract digits from username (supports: 700, PH906-700, ph906-700, etc.)
    $digits = preg_replace('/\D+/', '', $ph906Raw);

    if ($digits === '') {
        http_response_code(400);
        echo json_encode(['status' => 'error', 'message' => 'Invalid username format']);
        exit;
    }

    error_log("Searching for ph906: $digits");

    // Try multiple matching strategies
    $row = null;

    // 1. Try exact match
    $stmt = $pdo->prepare("SELECT ph906, first_name, last_name, birthday FROM masterlist WHERE ph906 = ? LIMIT 1");
    $stmt->execute([$ph906Raw]);
    $row = $stmt->fetch(PDO::FETCH_ASSOC);

    // 2. Try digits only
    if (!$row) {
        $stmt = $pdo->prepare("SELECT ph906, first_name, last_name, birthday FROM masterlist WHERE ph906 = ? LIMIT 1");
        $stmt->execute([$digits]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
    }

    // 3. Try with PH906- prefix
    if (!$row) {
        $withPrefix = "PH906-" . $digits;
        $stmt = $pdo->prepare("SELECT ph906, first_name, last_name, birthday FROM masterlist WHERE ph906 = ? LIMIT 1");
        $stmt->execute([$withPrefix]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
    }

    // 4. Try stripping all non-digits from DB values and matching
    if (!$row) {
        $stmt = $pdo->prepare("SELECT ph906, first_name, last_name, birthday FROM masterlist WHERE REPLACE(REPLACE(REPLACE(ph906, 'PH906-', ''), 'PH906', ''), '-', '') = ? LIMIT 1");
        $stmt->execute([$digits]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
    }

    if (!$row) {
        error_log("Student not found for: $digits");
        http_response_code(404);
        echo json_encode(['status' => 'error', 'message' => 'Account not found']);
        exit;
    }

    $ph906 = (string)$row['ph906'];
    $dbBirthday = (string)($row['birthday'] ?? '');
    $name = trim(($row['first_name'] ?? '') . ' ' . ($row['last_name'] ?? ''));

    error_log("Found student: $ph906 - $name, birthday in DB: $dbBirthday");

    // Validate birthday
    $normInput = normalizeDateToYmd($birthday);
    $normDb = normalizeDateToYmd($dbBirthday);

    error_log("Birthday comparison - Input: $normInput, DB: $normDb");

    $match = false;
    if ($normInput && $normDb && $normInput === $normDb) {
        $match = true;
    } elseif (substr($dbBirthday, 0, 10) === substr($birthday, 0, 10)) {
        $match = true;
    } elseif (substr($dbBirthday, 0, 10) === $birthday) {
        $match = true;
    }

    if (!$match) {
        error_log("Birthday mismatch for $ph906");
        http_response_code(401);
        echo json_encode(['status' => 'error', 'message' => 'Invalid credentials']);
        exit;
    }

    error_log("Birthday validated successfully");

    // Ensure reset_tokens table exists
    $pdo->exec("CREATE TABLE IF NOT EXISTS reset_tokens (
        id INT AUTO_INCREMENT PRIMARY KEY,
        ph906 VARCHAR(255) NOT NULL,
        token VARCHAR(255) NOT NULL UNIQUE,
        expires_at DATETIME NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        INDEX idx_ph906 (ph906),
        INDEX idx_expires_at (expires_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

    // Delete any previous token for this user
    error_log("Deleting old tokens for ph906: $ph906");
    $pdo->prepare("DELETE FROM reset_tokens WHERE ph906=?")->execute([$ph906]);

    // Generate secure random token
    try {
        $token = bin2hex(random_bytes(16)); // 32 character hex string
    } catch (Throwable $e) {
        $token = bin2hex(openssl_random_pseudo_bytes(16));
    }

    $ttl = 30 * 60; // 30 minutes
    $expiresAt = date('Y-m-d H:i:s', time() + $ttl);

    error_log("Generated token for $ph906: " . substr($token, 0, 8) . "... expires: $expiresAt");

    // Insert token
    $stmt = $pdo->prepare("INSERT INTO reset_tokens (ph906, token, expires_at) VALUES (?, ?, ?)");
    $stmt->execute([$ph906, $token, $expiresAt]);

    error_log("Token saved successfully");

    // Return token (in production, you'd send this via email/SMS instead)
    echo json_encode([
        'status' => 'success',
        'message' => 'Password reset token has been generated',
        'reset_token' => $token,
        'ph906' => $ph906,
        'name' => $name,
        'expires_at' => $expiresAt
    ], JSON_UNESCAPED_UNICODE);

} catch (Throwable $e) {
    error_log("ERROR in request_password_reset: " . $e->getMessage());
    error_log("Stack trace: " . $e->getTraceAsString());

    http_response_code(500);
    echo json_encode([
        'status' => 'error',
        'message' => 'Failed to generate reset token',
        'debug' => $e->getMessage()
    ]);
}

