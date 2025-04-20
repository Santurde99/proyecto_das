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

// No necesitamos parámetros de entrada para esta consulta
$query = "SELECT * FROM upgrades";
$result = mysqli_query($con, $query);

if (!$result) {
    echo json_encode(['status' => 'error', 'message' => 'Query failed: ' . mysqli_error($con)]);
    exit();
}

$upgrades = [];
while ($row = mysqli_fetch_assoc($result)) {
    $upgrades[] = $row;
}

echo json_encode([
    'status' => 'success',
    'data' => $upgrades
]);

mysqli_close($con);
?>