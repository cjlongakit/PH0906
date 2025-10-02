<?php
// request_password_reset.php
// Generates a reset token (or OTP) for the provided identifier (ph906 or email) and
// returns it as JSON (for testing). In production, you should send the token via
// email/SMS and avoid returning it in the response.

ini_set('display_errors', 0);
error_reporting(E_ALL);
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');

require_once __DIR__ . '/config.php'; // PDO $pdo
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
    $ph906Raw = $d['ph906'] ?? '';
    $email    = $d['email'] ?? '';

    if ($ph906Raw === '' && $email === '') {
        http_response_code(400);
        echo json_encode(['status' => 'error', 'message' => 'Provide ph906 or email']);
        exit;
    }

    // Resolve ph906 (prefer direct ph906 lookup)
    $ph906 = null;
    if ($ph906Raw !== '') {
        // normalize: keep digits only
        $digits = preg_replace('/[^0-9]/', '', $ph906Raw);
        // in DB, ph906 likely stored without prefix, but your data shows strings like "PH906-0700".
        // If masterlist stores the full value, prefer exact match; otherwise try digits only if needed.
        // First try exact match:
        $stmt = $pdo->prepare("SELECT ph906 FROM masterlist WHERE ph906=? LIMIT 1");
        $stmt->execute([$ph906Raw]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        if ($row) {
            $ph906 = $row['ph906'];
        } else {
            // Try digits only variant
            $stmt = $pdo->prepare("SELECT ph906 FROM masterlist WHERE REPLACE(REPLACE(ph906, 'PH906-', ''), '-', '')=? LIMIT 1");
            $stmt->execute([$digits]);
            $row2 = $stmt->fetch(PDO::FETCH_ASSOC);
            if ($row2) $ph906 = $row2['ph906'];
        }
    } elseif ($email !== '') {
        // Optional: only works if you have an `email` column in masterlist
        try {
            $stmt = $pdo->prepare("SELECT ph906 FROM masterlist WHERE email=? LIMIT 1");
            $stmt->execute([$email]);
            $row = $stmt->fetch(PDO::FETCH_ASSOC);
            if ($row) $ph906 = $row['ph906'];
        } catch (Throwable $e) {
            // Column missing -> tell client to use ph906 instead
            http_response_code(400);
            echo json_encode(['status'=>'error','message'=>'Email lookup not supported on server. Please use PH906 id.']);
            exit;
        }
    }

    if ($ph906 === null) {
        http_response_code(404);
        echo json_encode(['status' => 'error', 'message' => 'Account not found']);
        exit;
    }

    // Ensure reset_tokens table exists
    $pdo->exec("CREATE TABLE IF NOT EXISTS reset_tokens (
        id INT AUTO_INCREMENT PRIMARY KEY,
        ph906 VARCHAR(255) NOT NULL,
        token VARCHAR(255) NOT NULL UNIQUE,
        expires_at DATETIME NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        INDEX (ph906)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

    // Generate a secure token and set 30-minute expiry
    try {
        $bytes = random_bytes(16);
        $token = bin2hex($bytes);
    } catch (Throwable $e) {
        $token = bin2hex(openssl_random_pseudo_bytes(16));
    }
    $expires = date('Y-m-d H:i:s', time() + 30 * 60);

    // Upsert latest token for this ph906
    $stmt = $pdo->prepare("INSERT INTO reset_tokens (ph906, token, expires_at) VALUES (?, ?, ?)");
    $stmt->execute([$ph906, $token, $expires]);

    // For testing: return token directly. In production, send via email/SMS.
    echo json_encode([
        'status' => 'success',
        'reset_token' => $token,
        'expires_at' => $expires,
        'ph906' => $ph906
    ]);
} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => 'Failed to create reset token', 'debug' => $e->getMessage()]);
}

