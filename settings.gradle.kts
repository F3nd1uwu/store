rootProject.name = "store"

include("auth-service")
include("order-service")
include("inventory-service")
include("contracts")

include("auth-service:domain")

include("auth-service:application")

include("order-service:domain")

include("order-service:application")

include("inventory-service:domain")

include("inventory-service:application")
