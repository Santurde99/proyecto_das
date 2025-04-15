<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);
header('Content-Type: application/json');

// Configuración de la conexión
$DB_SERVER = "localhost";
$DB_USER = "Xagutierrez186";
$DB_PASS = "tDEInFYPT";
$DB_DATABASE = "Xagutierrez186_GoldDigger";

$con = mysqli_connect($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);

if (mysqli_connect_error()) {
    echo json_encode(['status' => 'error', 'message' => 'Database connection error']);
    exit();
}

$data = json_decode(file_get_contents('php://input'), true);
$action = $data['action'] ?? null;
$username = isset($data['username']) ? mysqli_real_escape_string($con, $data['username']) : null;


if (!$action || !$username) {
    echo json_encode(['valid' => false]);
    exit();
}


switch ($action) {
    case 'load':

        $query = "SELECT * FROM upgrades WHERE username = ?";
        $stmt = mysqli_prepare($con, $query);
        mysqli_stmt_bind_param($stmt, "s", $username);
        mysqli_stmt_execute($stmt);
        $result = mysqli_stmt_get_result($stmt);

        $upgrades = [];
        while ($row = mysqli_fetch_assoc($result)) {
            $upgrades[] = $row;
        }

        echo json_encode(['status' => 'success', 'data' => $upgrades]);
        break;

    case 'save':

        $upgradeId = (int)$data['upgrade_id'];
        $status = (int)$data['status'];

        $query = "UPDATE user_upgrades SET status = ? WHERE username = ? AND upgrade_id = ?";
        $stmt = mysqli_prepare($con, $query);
        mysqli_stmt_bind_param($stmt, "isi", $status, $username, $upgradeId);

        if (mysqli_stmt_execute($stmt)) {
            echo json_encode(['status' => 'success', 'message' => 'Upgrade saved']);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'Save failed']);
        }
        break;

    case 'new':

        $query = "INSERT INTO user_upgrades (username, upgrade_id, status) VALUES (?, ?, ?)";
        $stmt = mysqli_prepare($con, $query);

        $success = true;

        // Insertamos los 13 upgrades
        for ($i = 1; $i <= 13; $i++) {
            $status = ($i == 1) ? 1 : 0; //La primera mejora debe de estar habilitada, las demas no
            mysqli_stmt_bind_param($stmt, "sii", $username, $i, $status);
            if (!mysqli_stmt_execute($stmt)) {
                $success = false;
                break;
            }
        }

        if ($success) {
            echo json_encode(['status' => 'success', 'message' => 'All upgrades created']);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'Creation failed for some upgrades']);
        }
        break;

    default:
        echo json_encode(['status' => 'error', 'message' => 'Invalid action']);
}

mysqli_close($con);
?>