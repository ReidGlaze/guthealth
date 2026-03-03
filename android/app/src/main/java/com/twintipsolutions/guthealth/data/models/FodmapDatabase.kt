package com.twintipsolutions.guthealth.data.models

/**
 * Local embedded FODMAP reference database based on Monash University data.
 * Provides offline-first food lookup without requiring Firestore connectivity.
 */
object FodmapDatabase {

    val bundledFoods: List<FodmapFood> = listOf(
        // Fruits
        FodmapFood(name = "Banana (unripe)", category = "fruit", fodmapLevel = "low", fodmapCategories = emptyList(), servingSize = "1 medium", lowFodmapServing = "1 medium", notes = "Ripe bananas have more fructans"),
        FodmapFood(name = "Banana (ripe)", category = "fruit", fodmapLevel = "moderate", fodmapCategories = listOf("fructans"), servingSize = "1 medium", lowFodmapServing = "1/3 medium", notes = "Ripeness increases FODMAP content"),
        FodmapFood(name = "Blueberries", category = "fruit", fodmapLevel = "low", servingSize = "1 cup", lowFodmapServing = "1 cup"),
        FodmapFood(name = "Strawberries", category = "fruit", fodmapLevel = "low", servingSize = "1 cup", lowFodmapServing = "1 cup"),
        FodmapFood(name = "Grapes", category = "fruit", fodmapLevel = "low", servingSize = "1 cup", lowFodmapServing = "1 cup"),
        FodmapFood(name = "Orange", category = "fruit", fodmapLevel = "low", servingSize = "1 medium", lowFodmapServing = "1 medium"),
        FodmapFood(name = "Apple", category = "fruit", fodmapLevel = "high", fodmapCategories = listOf("fructose", "sorbitol"), servingSize = "1 medium", lowFodmapServing = "Avoid or 1/4", notes = "Common trigger food"),
        FodmapFood(name = "Pear", category = "fruit", fodmapLevel = "high", fodmapCategories = listOf("fructose", "sorbitol"), servingSize = "1 medium", lowFodmapServing = "Avoid or 1/4", notes = "Common trigger food"),
        FodmapFood(name = "Mango", category = "fruit", fodmapLevel = "high", fodmapCategories = listOf("fructose"), servingSize = "1 cup", lowFodmapServing = "1/4 cup"),
        FodmapFood(name = "Watermelon", category = "fruit", fodmapLevel = "high", fodmapCategories = listOf("fructose", "mannitol", "fructans"), servingSize = "1 cup", lowFodmapServing = "Avoid", notes = "Triple FODMAP threat"),
        FodmapFood(name = "Cherries", category = "fruit", fodmapLevel = "high", fodmapCategories = listOf("fructose", "sorbitol"), servingSize = "1 cup", lowFodmapServing = "3 cherries"),
        FodmapFood(name = "Avocado", category = "fruit", fodmapLevel = "moderate", fodmapCategories = listOf("sorbitol"), servingSize = "1/2 avocado", lowFodmapServing = "1/8 avocado", notes = "Small amounts OK"),
        FodmapFood(name = "Kiwi", category = "fruit", fodmapLevel = "low", servingSize = "2 small", lowFodmapServing = "2 small", notes = "Good for digestion"),
        FodmapFood(name = "Pineapple", category = "fruit", fodmapLevel = "low", servingSize = "1 cup", lowFodmapServing = "1 cup", notes = "Contains bromelain enzyme"),

        // Vegetables
        FodmapFood(name = "Garlic", category = "vegetable", fodmapLevel = "high", fodmapCategories = listOf("fructans"), servingSize = "1 clove", lowFodmapServing = "Use garlic-infused oil instead", notes = "Top IBS trigger. Fructans are water-soluble, not oil-soluble"),
        FodmapFood(name = "Onion", category = "vegetable", fodmapLevel = "high", fodmapCategories = listOf("fructans", "GOS"), servingSize = "1/2 cup", lowFodmapServing = "Use green part of spring onion", notes = "Top IBS trigger"),
        FodmapFood(name = "Broccoli", category = "vegetable", fodmapLevel = "low", servingSize = "1 cup heads", lowFodmapServing = "3/4 cup", notes = "Stalks are higher FODMAP than heads"),
        FodmapFood(name = "Cauliflower", category = "vegetable", fodmapLevel = "moderate", fodmapCategories = listOf("mannitol"), servingSize = "1 cup", lowFodmapServing = "1/2 cup"),
        FodmapFood(name = "Bell Pepper", category = "vegetable", fodmapLevel = "low", servingSize = "1 cup", lowFodmapServing = "1 cup", notes = "All colors are low FODMAP"),
        FodmapFood(name = "Carrot", category = "vegetable", fodmapLevel = "low", servingSize = "1 medium", lowFodmapServing = "1 medium"),
        FodmapFood(name = "Spinach", category = "vegetable", fodmapLevel = "low", servingSize = "2 cups raw", lowFodmapServing = "2 cups raw"),
        FodmapFood(name = "Tomato", category = "vegetable", fodmapLevel = "low", servingSize = "1 medium", lowFodmapServing = "1 medium", notes = "Cherry tomatoes are moderate in larger amounts"),
        FodmapFood(name = "Zucchini", category = "vegetable", fodmapLevel = "low", servingSize = "1 cup", lowFodmapServing = "1/2 cup"),
        FodmapFood(name = "Mushrooms", category = "vegetable", fodmapLevel = "high", fodmapCategories = listOf("mannitol"), servingSize = "1 cup", lowFodmapServing = "Avoid or 2-3 pieces", notes = "Most varieties are high FODMAP"),
        FodmapFood(name = "Asparagus", category = "vegetable", fodmapLevel = "moderate", fodmapCategories = listOf("fructans"), servingSize = "5 spears", lowFodmapServing = "1 spear"),
        FodmapFood(name = "Sweet Potato", category = "vegetable", fodmapLevel = "moderate", fodmapCategories = listOf("mannitol"), servingSize = "1 cup", lowFodmapServing = "1/2 cup"),
        FodmapFood(name = "Potato", category = "vegetable", fodmapLevel = "low", servingSize = "1 medium", lowFodmapServing = "1 medium", notes = "All varieties are low FODMAP"),
        FodmapFood(name = "Celery", category = "vegetable", fodmapLevel = "moderate", fodmapCategories = listOf("mannitol"), servingSize = "1 stalk", lowFodmapServing = "1/4 stalk"),
        FodmapFood(name = "Cucumber", category = "vegetable", fodmapLevel = "low", servingSize = "1 cup", lowFodmapServing = "1 cup"),
        FodmapFood(name = "Green Beans", category = "vegetable", fodmapLevel = "low", servingSize = "1 cup", lowFodmapServing = "3/4 cup"),
        FodmapFood(name = "Cabbage (common)", category = "vegetable", fodmapLevel = "low", servingSize = "1 cup", lowFodmapServing = "3/4 cup", notes = "Savoy cabbage is higher"),
        FodmapFood(name = "Eggplant", category = "vegetable", fodmapLevel = "low", servingSize = "1 cup", lowFodmapServing = "1 cup"),

        // Grains
        FodmapFood(name = "White Rice", category = "grain", fodmapLevel = "low", servingSize = "1 cup cooked", lowFodmapServing = "1 cup cooked", notes = "Safest grain for IBS"),
        FodmapFood(name = "Brown Rice", category = "grain", fodmapLevel = "low", servingSize = "1 cup cooked", lowFodmapServing = "1 cup cooked"),
        FodmapFood(name = "Oats", category = "grain", fodmapLevel = "low", servingSize = "1/2 cup dry", lowFodmapServing = "1/2 cup dry", notes = "Gluten-free oats recommended"),
        FodmapFood(name = "Quinoa", category = "grain", fodmapLevel = "low", servingSize = "1 cup cooked", lowFodmapServing = "1 cup cooked"),
        FodmapFood(name = "Wheat Bread", category = "grain", fodmapLevel = "high", fodmapCategories = listOf("fructans"), servingSize = "2 slices", lowFodmapServing = "1 slice sourdough spelt", notes = "Sourdough process reduces fructans"),
        FodmapFood(name = "Wheat Pasta", category = "grain", fodmapLevel = "high", fodmapCategories = listOf("fructans"), servingSize = "1 cup cooked", lowFodmapServing = "1/2 cup cooked or use gluten-free"),
        FodmapFood(name = "Sourdough Bread (spelt)", category = "grain", fodmapLevel = "low", servingSize = "2 slices", lowFodmapServing = "2 slices", notes = "Fermentation reduces fructans"),
        FodmapFood(name = "Corn/Polenta", category = "grain", fodmapLevel = "low", servingSize = "1 cup", lowFodmapServing = "1 cup"),

        // Dairy
        FodmapFood(name = "Milk (cow)", category = "dairy", fodmapLevel = "high", fodmapCategories = listOf("lactose"), servingSize = "1 cup", lowFodmapServing = "Use lactose-free"),
        FodmapFood(name = "Lactose-Free Milk", category = "dairy", fodmapLevel = "low", servingSize = "1 cup", lowFodmapServing = "1 cup"),
        FodmapFood(name = "Hard Cheese (cheddar, parmesan)", category = "dairy", fodmapLevel = "low", servingSize = "1 oz", lowFodmapServing = "2 oz", notes = "Aging removes lactose"),
        FodmapFood(name = "Soft Cheese (ricotta, cottage)", category = "dairy", fodmapLevel = "high", fodmapCategories = listOf("lactose"), servingSize = "1/2 cup", lowFodmapServing = "2 tablespoons"),
        FodmapFood(name = "Yogurt (regular)", category = "dairy", fodmapLevel = "high", fodmapCategories = listOf("lactose"), servingSize = "1 cup", lowFodmapServing = "Use lactose-free yogurt"),
        FodmapFood(name = "Butter", category = "dairy", fodmapLevel = "low", servingSize = "1 tablespoon", lowFodmapServing = "1 tablespoon", notes = "Minimal lactose"),
        FodmapFood(name = "Ice Cream", category = "dairy", fodmapLevel = "high", fodmapCategories = listOf("lactose"), servingSize = "1/2 cup", lowFodmapServing = "Use lactose-free or sorbet"),

        // Protein
        FodmapFood(name = "Chicken", category = "protein", fodmapLevel = "low", servingSize = "1 palm size", lowFodmapServing = "No limit", notes = "Plain, unprocessed"),
        FodmapFood(name = "Fish", category = "protein", fodmapLevel = "low", servingSize = "1 fillet", lowFodmapServing = "No limit", notes = "Plain, unprocessed"),
        FodmapFood(name = "Eggs", category = "protein", fodmapLevel = "low", servingSize = "2 eggs", lowFodmapServing = "No limit"),
        FodmapFood(name = "Beef", category = "protein", fodmapLevel = "low", servingSize = "1 palm size", lowFodmapServing = "No limit", notes = "Plain, unprocessed"),
        FodmapFood(name = "Tofu (firm)", category = "protein", fodmapLevel = "low", servingSize = "1/2 cup", lowFodmapServing = "2/3 cup", notes = "Firm/extra-firm only. Silken tofu is high"),

        // Legumes
        FodmapFood(name = "Lentils", category = "legume", fodmapLevel = "high", fodmapCategories = listOf("GOS", "fructans"), servingSize = "1 cup", lowFodmapServing = "1/4 cup canned, drained", notes = "Canning reduces FODMAPs"),
        FodmapFood(name = "Chickpeas", category = "legume", fodmapLevel = "high", fodmapCategories = listOf("GOS", "fructans"), servingSize = "1 cup", lowFodmapServing = "1/4 cup canned, drained", notes = "Canning reduces FODMAPs"),
        FodmapFood(name = "Black Beans", category = "legume", fodmapLevel = "high", fodmapCategories = listOf("GOS"), servingSize = "1 cup", lowFodmapServing = "1/4 cup canned, drained"),
        FodmapFood(name = "Edamame", category = "legume", fodmapLevel = "moderate", fodmapCategories = listOf("GOS"), servingSize = "1 cup", lowFodmapServing = "1/2 cup"),

        // Nuts & Seeds
        FodmapFood(name = "Almonds", category = "nuts", fodmapLevel = "low", servingSize = "10 almonds", lowFodmapServing = "10 almonds", notes = "Larger portions are moderate"),
        FodmapFood(name = "Cashews", category = "nuts", fodmapLevel = "high", fodmapCategories = listOf("GOS"), servingSize = "1/4 cup", lowFodmapServing = "Avoid or 10 nuts"),
        FodmapFood(name = "Walnuts", category = "nuts", fodmapLevel = "low", servingSize = "10 halves", lowFodmapServing = "10 halves"),
        FodmapFood(name = "Pistachios", category = "nuts", fodmapLevel = "high", fodmapCategories = listOf("fructans", "GOS"), servingSize = "1/4 cup", lowFodmapServing = "Avoid"),
        FodmapFood(name = "Peanuts", category = "nuts", fodmapLevel = "low", servingSize = "32 nuts", lowFodmapServing = "32 nuts"),
        FodmapFood(name = "Pumpkin Seeds", category = "nuts", fodmapLevel = "low", servingSize = "2 tablespoons", lowFodmapServing = "2 tablespoons"),
        FodmapFood(name = "Chia Seeds", category = "nuts", fodmapLevel = "low", servingSize = "2 tablespoons", lowFodmapServing = "2 tablespoons", notes = "High in soluble fiber"),

        // Sweeteners
        FodmapFood(name = "Honey", category = "sweetener", fodmapLevel = "high", fodmapCategories = listOf("fructose"), servingSize = "1 tablespoon", lowFodmapServing = "Avoid. Use maple syrup", notes = "Very high in excess fructose"),
        FodmapFood(name = "Maple Syrup", category = "sweetener", fodmapLevel = "low", servingSize = "2 tablespoons", lowFodmapServing = "2 tablespoons", notes = "Best sweetener for IBS"),
        FodmapFood(name = "Table Sugar", category = "sweetener", fodmapLevel = "low", servingSize = "1 tablespoon", lowFodmapServing = "1 tablespoon", notes = "Sucrose is low FODMAP in normal amounts"),
        FodmapFood(name = "High Fructose Corn Syrup", category = "sweetener", fodmapLevel = "high", fodmapCategories = listOf("fructose"), servingSize = "any", lowFodmapServing = "Avoid", notes = "Check labels on processed foods"),
        FodmapFood(name = "Sorbitol (artificial sweetener)", category = "sweetener", fodmapLevel = "high", fodmapCategories = listOf("sorbitol"), servingSize = "any", lowFodmapServing = "Avoid", notes = "Found in sugar-free gum and candy"),

        // Beverages
        FodmapFood(name = "Coffee (black)", category = "beverage", fodmapLevel = "low", servingSize = "1 cup", lowFodmapServing = "1 cup", notes = "Can still irritate gut via caffeine"),
        FodmapFood(name = "Tea (black/green)", category = "beverage", fodmapLevel = "low", servingSize = "1 cup", lowFodmapServing = "1 cup", notes = "Weak brew. Strong brew may be moderate"),
        FodmapFood(name = "Chamomile Tea", category = "beverage", fodmapLevel = "high", fodmapCategories = listOf("fructans"), servingSize = "1 cup strong", lowFodmapServing = "1 cup weak"),
        FodmapFood(name = "Almond Milk", category = "beverage", fodmapLevel = "low", servingSize = "1 cup", lowFodmapServing = "1 cup", notes = "Check for added high-FODMAP sweeteners"),
        FodmapFood(name = "Oat Milk", category = "beverage", fodmapLevel = "moderate", fodmapCategories = listOf("GOS"), servingSize = "1 cup", lowFodmapServing = "1/2 cup"),
        FodmapFood(name = "Coconut Water", category = "beverage", fodmapLevel = "moderate", fodmapCategories = listOf("sorbitol"), servingSize = "1 cup", lowFodmapServing = "1/2 cup"),

        // Condiments
        FodmapFood(name = "Soy Sauce", category = "condiment", fodmapLevel = "low", servingSize = "2 tablespoons", lowFodmapServing = "2 tablespoons"),
        FodmapFood(name = "Olive Oil", category = "condiment", fodmapLevel = "low", servingSize = "1 tablespoon", lowFodmapServing = "1 tablespoon", notes = "All oils are FODMAP-free"),
        FodmapFood(name = "Garlic-Infused Oil", category = "condiment", fodmapLevel = "low", servingSize = "1 tablespoon", lowFodmapServing = "1 tablespoon", notes = "Fructans are water-soluble, not oil-soluble. Safe substitute for garlic"),
        FodmapFood(name = "Ketchup", category = "condiment", fodmapLevel = "low", servingSize = "1 tablespoon", lowFodmapServing = "1 sachet", notes = "Check for HFCS"),
        FodmapFood(name = "Mayonnaise", category = "condiment", fodmapLevel = "low", servingSize = "2 tablespoons", lowFodmapServing = "2 tablespoons"),
    )

}
