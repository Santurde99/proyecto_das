<?php

// Activar reporte de errores (solo para desarrollo)
error_reporting(E_ALL);  // Reporta todos los errores
ini_set('display_errors', 1);  // Muestra errores en pantalla
ini_set('display_startup_errors', 1);  // Muestra errores de inicio

header('Content-Type: application/json');

// Configuración de la conexión a la base de datos
$DB_SERVER = "localhost";
$DB_USER = "Xagutierrez186";
$DB_PASS = "tDEInFYPT";
$DB_DATABASE = "Xagutierrez186_GoldDigger";

$con = mysqli_connect($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);

// Comprobar conexión
if (mysqli_connect_error()) {
    echo json_encode(['status' => 'error', 'message' => 'Database connection error']);
    exit();
}

// Recibir datos JSON
$data = json_decode(file_get_contents('php://input'), true);
$action = isset($data['action']) ? $data['action'] : null;
$username = isset($data['username']) ? mysqli_real_escape_string($con, $data['username']) : null;
$password = isset($data['password']) ? $data['password'] : null;
$image_data = isset($data['image_data']) ? $data['image_data'] : null;

// Validación básica
if (!$action || !$username) {
    echo json_encode(['status' => 'error', 'message' => 'Action and username are required']);
    exit();
}

switch ($action) {
    case 'login':
        if (!$password) {
            echo json_encode(['status' => 'error', 'message' => 'Password is required for login']);
            exit();
        }

        // Buscar usuario en la base de datos
        $query = "SELECT password FROM users WHERE username = ?";
        $stmt = mysqli_prepare($con, $query);
        mysqli_stmt_bind_param($stmt, "s", $username);
        mysqli_stmt_execute($stmt);
        $result = mysqli_stmt_get_result($stmt);

        if (mysqli_num_rows($result) == 0) {
            echo json_encode(['status' => 'error', 'message' => 'User not found']);
            exit();
        }

        $user = mysqli_fetch_assoc($result);
        $hashed_password = $user['password'];

        // Verificar contraseña y devolver solo true/false
        echo json_encode([
            'status' => password_verify($password, $hashed_password) ? 'success' : 'error',
            'valid' => password_verify($password, $hashed_password)
        ]);
        break;

    case 'register':
        if (!$password) {
            echo json_encode([
                'status' => 'error',
                'message' => 'Password is required for registration'
            ]);
            exit();
        }

        // Verificar si el usuario ya existe
        $query = "SELECT * FROM users WHERE username = ?";
        $stmt = mysqli_prepare($con, $query);
        mysqli_stmt_bind_param($stmt, "s", $username);
        mysqli_stmt_execute($stmt);
        mysqli_stmt_store_result($stmt);

        if (mysqli_stmt_num_rows($stmt) > 0) {
            echo json_encode([
                'status' => 'error',
                'message' => 'Username already exists'
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
                'message' => 'User registered successfully',
            ]);
        } else {
            echo json_encode([
                'status' => 'error',
                'message' => 'Error registering user: '. mysqli_error($con)
            ]);
        }
        break;

    case 'load_profile_pic':
        $query = "SELECT profile_pic FROM users WHERE username = ?";
        $stmt = mysqli_prepare($con, $query);
        mysqli_stmt_bind_param($stmt, "s", $username);
        mysqli_stmt_execute($stmt);

        // Bindear el resultado directamente
        mysqli_stmt_bind_result($stmt, $profile_pic);
        mysqli_stmt_fetch($stmt);

        if ($profile_pic !== null) {
            // Convertir el BLOB a base64
            $base64_image = base64_encode($profile_pic);
            echo json_encode([
                'status' => 'success',
                'image_data' => $base64_image
            ]);
        } else {
            echo json_encode([
                'status' => 'success',
                'image_data' => '' // Cadena vacía en lugar de null
            ]);
        }
        break;

    case 'save_profile_pic':
        if (!$image_data) {
            echo json_encode([
                'status' => 'error',
                'message' => 'Image data is required'
            ]);
            exit();
        }

        $query = "SELECT * FROM users WHERE username = ?";
        $stmt = mysqli_prepare($con, $query);
        mysqli_stmt_bind_param($stmt, "s", $username);
        mysqli_stmt_execute($stmt);
        mysqli_stmt_store_result($stmt);

        if (mysqli_stmt_num_rows($stmt) == 0) {
            echo json_encode([
                'status' => 'error',
                'message' => 'User not found'
            ]);
            exit();
        }

        // Decodificar el base64 a binario
        $binary_image = base64_decode($image_data);
        if ($binary_image === false) {
            echo json_encode([
                'status' => 'error',
                'message' => 'Invalid image data format'
            ]);
            exit();
        }

        $query = "UPDATE users SET profile_pic = ? WHERE username = ?";
        $stmt = mysqli_prepare($con, $query);

        $null = NULL;
        mysqli_stmt_bind_param($stmt, "bs", $null, $username);
        mysqli_stmt_send_long_data($stmt, 0, $binary_image);

        if (mysqli_stmt_execute($stmt)) {
            echo json_encode([
                'status' => 'success',
                'message' => 'Profile picture updated successfully'
            ]);
        } else {
            echo json_encode([
                'status' => 'error',
                'message' => 'Error updating profile picture: '. mysqli_error($con)
            ]);
        }
        break;

    default:
        echo json_encode([
            'status' => 'error',
            'message' => 'Invalid action'
        ]);
}

mysqli_close($con);
?>