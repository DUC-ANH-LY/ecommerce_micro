print("Starting data generation...");

// Create Categories
var categories = [];
for (var i = 1; i <= 100; i++) {
    categories.push({
        _id: "cat" + i,
        name: "Category " + i,
        description: "Description for category " + i,
        _class: "com.ecommerce.product_service.entity.Category"
    });
}
db.categories.insertMany(categories);
print("Inserted 100 categories.");

// Create Products in batches to avoid memory issues
var batchSize = 10000;
var totalProducts = 1000000;
var products = [];

for (var i = 1; i <= totalProducts; i++) {
    var catId1 = "cat" + (Math.floor(Math.random() * 100) + 1);
    var catId2 = "cat" + (Math.floor(Math.random() * 100) + 1);
    var cats = [
        { "$ref": "categories", "$id": catId1 }
    ];
    if (catId1 !== catId2) {
        cats.push({ "$ref": "categories", "$id": catId2 });
    }
    
    products.push({
        name: "Product " + i,
        description: "Description for product " + i,
        price: Math.floor(Math.random() * 1000 + 10) + 0.99,
        stock: Math.floor(Math.random() * 500 + 1),
        categories: cats,
        _class: "com.ecommerce.product_service.entity.Product"
    });

    if (i % batchSize === 0) {
        db.products.insertMany(products);
        products = [];
        if (i % 100000 === 0) {
            print("Inserted " + i + " products.");
        }
    }
}
// Create an index for performance tests
db.products.createIndex({ "price": 1 });
print("Finished inserting 1,000,000 products.");
