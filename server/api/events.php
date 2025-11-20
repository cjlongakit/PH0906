<?php
declare(strict_types=1);

ini_set('display_errors', '0');
error_reporting(E_ALL);
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Content-Type, Authorization');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
header('Pragma: no-cache');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

require_once __DIR__ . '/config.php';

$date     = $_GET['date']     ?? $_POST['date']     ?? '';
$month    = $_GET['month']    ?? $_POST['month']    ?? '';
$upcoming = $_GET['upcoming'] ?? $_POST['upcoming'] ?? '';
$from     = $_GET['from']     ?? $_POST['from']     ?? '';
$to       = $_GET['to']       ?? $_POST['to']       ?? '';
$limitArg = $_GET['limit']    ?? $_POST['limit']    ?? '';

$limit = (int)$limitArg;
if ($limit <= 0 || $limit > 1000) $limit = 0;

$where = [];
$params = [];

// 🎯 If exact date provided
if ($date && preg_match('/^\d{4}-\d{2}-\d{2}$/', $date)) {
    $where[] = 'DATE(event_date) = :d';
    $params[':d'] = $date;
}
// 🎯 If month provided
elseif ($month && preg_match('/^\d{4}-\d{2}$/', $month)) {
    [$y, $m] = array_map('intval', explode('-', $month));
    $start = sprintf('%04d-%02d-01', $y, $m);
    $end   = $m === 12 ? sprintf('%04d-01-01', $y + 1)
                       : sprintf('%04d-%02d-01', $y, $m + 1);
    $where[] = 'DATE(event_date) >= :start AND DATE(event_date) < :end';
    $params[':start'] = $start;
    $params[':end']   = $end;
}
// 🎯 If date range provided (for fallback compatibility)
elseif ($from && $to) {
    $where[] = 'DATE(event_date) >= :from AND DATE(event_date) <= :to';
    $params[':from'] = $from;
    $params[':to'] = $to;
}
// 🎯 Default: upcoming (today and future)
else {
    $where[] = 'DATE(event_date) >= CURDATE()';
}

$sql = 'SELECT id, event_title, event_date FROM events';
if ($where) $sql .= ' WHERE ' . implode(' AND ', $where);
$sql .= ' ORDER BY DATE(event_date) ASC, id ASC';
if ($limit > 0) $sql .= ' LIMIT ' . $limit;

try {
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $out = [];
    foreach ($rows as $r) {
        $eventDate = $r['event_date'];
        // Extract just the date part if it's a datetime
        if (strpos($eventDate, ' ') !== false) {
            $datePart = explode(' ', $eventDate)[0];
        } else {
            $datePart = $eventDate;
        }

        $out[] = [
            'id'          => (int)$r['id'],
            'title'       => (string)$r['event_title'],
            'start'       => $datePart,
            'end'         => $datePart,
            'event_title' => (string)$r['event_title'],
            'event_date'  => $datePart,
            'date'        => $datePart, // ✅ CRITICAL: This field is required for SyncWorker notifications!
        ];
    }

    echo json_encode(['data' => $out], JSON_UNESCAPED_UNICODE);
} catch (Throwable $e) {
    error_log("Events query error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Query failed', 'details' => $e->getMessage()]);
}

