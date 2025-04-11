<?php
header('Content-Type: application/json');

// Configuración de la conexión a la base de datos
$DB_SERVER = "localhost";
$DB_USER = "Xagutierrez186";
$DB_PASS = "tDEInFYPT";
$DB_DATABASE = "Xagutierrez186_GoldDigger";

$con = mysqli_connect($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);

// Comprobar conexión
if (mysqli_connect_error()) {
    echo json_encode(['valid' => false]);
    exit();
}

// Recibir datos JSON
$data = json_decode(file_get_contents('php://input'), true);
$action = isset($data['action']) ? $data['action'] : null;
$username = isset($data['username']) ? mysqli_real_escape_string($con, $data['username']) : null;
$password = isset($data['password']) ? $data['password'] : null;

// Validación básica
if (!$action || !$username) {
    echo json_encode(['valid' => false]);
    exit();
}

switch ($action) {
    case 'login':
        if (!$password) {
            echo json_encode(['valid' => false]);
            exit();
        }

        // Buscar usuario en la base de datos
        $query = "SELECT password FROM users WHERE username = ?";
        $stmt = mysqli_prepare($con, $query);
        mysqli_stmt_bind_param($stmt, "s", $username);
        mysqli_stmt_execute($stmt);
        $result = mysqli_stmt_get_result($stmt);

        if (mysqli_num_rows($result) == 0) {
            echo json_encode(['valid' => false]);
            exit();
        }

        $user = mysqli_fetch_assoc($result);
        $hashed_password = $user['password'];

        // Verificar contraseña y devolver solo true/false
        echo json_encode([
            'valid' => password_verify($password, $hashed_password)
        ]);
        break;

    case 'register':
        if (!$password) {
            echo json_encode([
                'status' => 'error',
                'message' => 'La contraseña es requerida para registro'
            ]);
            exit();
        }

        // Verificar si el usuario ya existe
        $query = "SELECT id FROM users WHERE username = ?";
        $stmt = mysqli_prepare($con, $query);
        mysqli_stmt_bind_param($stmt, "s", $username);
        mysqli_stmt_execute($stmt);
        mysqli_stmt_store_result($stmt);

        if (mysqli_stmt_num_rows($stmt) > 0) {
            echo json_encode([
                'status' => 'error',
                'message' => 'El usuario ya existe'
            ]);
            exit();
        }

        // Insertar nuevo usuario con contraseña hasheada
        $hashed_password = password_hash($password, PASSWORD_DEFAULT);
        $query = "INSERT INTO users (username, password) VALUES (?, ?)";
        $stmt = mysqli_prepare($con, $query);
        mysqli_stmt_bind_param($stmt, "ss", $username, $hashed_password);

        if (mysqli_stmt_execute($stmt)) {
            echo json_encode([
                'status' => 'success',
                'message' => 'Usuario registrado correctamente',
                'user_id' => mysqli_insert_id($con)
            ]);
        } else {
            echo json_encode([
                'status' => 'error',
                'message' => 'Error al registrar el usuario: '. mysqli_error($con)
            ]);
        }
        break;

    default:
        echo json_encode(['valid' => false]);
}

mysqli_close($con);
?>