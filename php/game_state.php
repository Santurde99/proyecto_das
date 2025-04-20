<?php
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
$action = isset($data['action']) ? $data['action'] : null;
$username = isset($data['username']) ? mysqli_real_escape_string($con, $data['username']) : null;

if (!$action || !$username) {
    echo json_encode(['valid' => false]);
    exit();
}

switch ($action) {
    case 'new':
        if (!$username) {
            echo json_encode(['status' => 'error', 'message' => 'Username is required']);
            break;
        }

        $query = "INSERT INTO game_state (username)
                 VALUES (?)";
        $stmt = mysqli_prepare($con, $query);
        mysqli_stmt_bind_param($stmt, "s", $username);

        if (mysqli_stmt_execute($stmt)) {
            echo json_encode(['status' => 'success', 'message' => 'User created']);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'Insert failed']);
        }
        break;

    case 'save':
        if (!$username) {
            echo json_encode(['status' => 'error', 'message' => 'Username is required']);
            break;
        }

        $points = $data['points'] ?? null;
        $clickPoints = $data['click_points'] ?? null;
        $passivePoints = $data['passive_points'] ?? null;
        $clickMultiplier = $data['click_multiplier'] ?? null;
        $passiveMultiplier = $data['passive_multiplier'] ?? null;

        $query = "UPDATE game_state SET
                 points = COALESCE(?, points),
                 click_points = COALESCE(?, click_points),
                 passive_points = COALESCE(?, passive_points),
                 click_multiplier = COALESCE(?, click_multiplier),
                 passive_multiplier = COALESCE(?, passive_multiplier)
                 WHERE username = ?";

        $stmt = mysqli_prepare($con, $query);
        mysqli_stmt_bind_param($stmt,"iiidds",$points, $clickPoints, $passivePoints,
                             $clickMultiplier, $passiveMultiplier, $username);

        if (mysqli_stmt_execute($stmt)) {
            echo json_encode(['status' => 'success', 'message' => 'User updated']);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'Update failed']);
        }
        break;

    case 'load':

        $query = "SELECT * FROM game_state WHERE username = ?";
        $stmt = mysqli_prepare($con, $query);
        mysqli_stmt_bind_param($stmt, "s", $username);
        mysqli_stmt_execute($stmt);
        $result = mysqli_stmt_get_result($stmt);

        if (mysqli_num_rows($result) > 0) {
            $user_data = mysqli_fetch_assoc($result);
            echo json_encode(['status' => 'success', 'data' => $user_data]);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'User not found']);
        }
        break;

    default:
        echo json_encode(['status' => 'error', 'message' => 'Invalid action']);
}

mysqli_close($con);
?>