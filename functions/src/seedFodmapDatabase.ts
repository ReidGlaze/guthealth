/**
 * Seed script for the FODMAP reference database.
 * Run with: cd functions && npx ts-node src/seedFodmapDatabase.ts
 *
 * Requires GOOGLE_APPLICATION_CREDENTIALS environment variable pointing to
 * a service account key, or run after `firebase login` with admin SDK.
 *
 * Based on Monash University FODMAP data.
 */

import * as admin from "firebase-admin";

admin.initializeApp();
const db = admin.firestore();

interface FodmapFood {
  name: string;
  category: string;
  fodmapLevel: "low" | "moderate" | "high";
  fodmapCategories: string[];
  servingSize: string;
  lowFodmapServing: string;
  notes: string;
}

const fodmapFoods: FodmapFood[] = [
  // ---- FRUITS ----
  { name: "Banana (unripe)", category: "fruit", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 medium", lowFodmapServing: "1 medium", notes: "Ripe bananas have more fructans" },
  { name: "Banana (ripe)", category: "fruit", fodmapLevel: "moderate", fodmapCategories: ["fructans"], servingSize: "1 medium", lowFodmapServing: "1/3 medium", notes: "Ripeness increases FODMAP content" },
  { name: "Blueberries", category: "fruit", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup", lowFodmapServing: "1 cup", notes: "" },
  { name: "Strawberries", category: "fruit", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup", lowFodmapServing: "1 cup", notes: "" },
  { name: "Grapes", category: "fruit", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup", lowFodmapServing: "1 cup", notes: "" },
  { name: "Orange", category: "fruit", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 medium", lowFodmapServing: "1 medium", notes: "" },
  { name: "Apple", category: "fruit", fodmapLevel: "high", fodmapCategories: ["fructose", "sorbitol"], servingSize: "1 medium", lowFodmapServing: "Avoid or 1/4", notes: "Common trigger food" },
  { name: "Pear", category: "fruit", fodmapLevel: "high", fodmapCategories: ["fructose", "sorbitol"], servingSize: "1 medium", lowFodmapServing: "Avoid or 1/4", notes: "Common trigger food" },
  { name: "Mango", category: "fruit", fodmapLevel: "high", fodmapCategories: ["fructose"], servingSize: "1 cup", lowFodmapServing: "1/4 cup", notes: "" },
  { name: "Watermelon", category: "fruit", fodmapLevel: "high", fodmapCategories: ["fructose", "mannitol", "fructans"], servingSize: "1 cup", lowFodmapServing: "Avoid", notes: "Triple FODMAP threat" },
  { name: "Cherries", category: "fruit", fodmapLevel: "high", fodmapCategories: ["fructose", "sorbitol"], servingSize: "1 cup", lowFodmapServing: "3 cherries", notes: "" },
  { name: "Avocado", category: "fruit", fodmapLevel: "moderate", fodmapCategories: ["sorbitol"], servingSize: "1/2 avocado", lowFodmapServing: "1/8 avocado", notes: "Small amounts OK" },
  { name: "Kiwi", category: "fruit", fodmapLevel: "low", fodmapCategories: [], servingSize: "2 small", lowFodmapServing: "2 small", notes: "Good for digestion" },
  { name: "Pineapple", category: "fruit", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup", lowFodmapServing: "1 cup", notes: "Contains bromelain enzyme" },

  // ---- VEGETABLES ----
  { name: "Garlic", category: "vegetable", fodmapLevel: "high", fodmapCategories: ["fructans"], servingSize: "1 clove", lowFodmapServing: "Use garlic-infused oil instead", notes: "Top IBS trigger. Fructans are water-soluble, not oil-soluble" },
  { name: "Onion", category: "vegetable", fodmapLevel: "high", fodmapCategories: ["fructans", "GOS"], servingSize: "1/2 cup", lowFodmapServing: "Use green part of spring onion", notes: "Top IBS trigger" },
  { name: "Broccoli", category: "vegetable", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup heads", lowFodmapServing: "3/4 cup", notes: "Stalks are higher FODMAP than heads" },
  { name: "Cauliflower", category: "vegetable", fodmapLevel: "moderate", fodmapCategories: ["mannitol"], servingSize: "1 cup", lowFodmapServing: "1/2 cup", notes: "" },
  { name: "Bell Pepper", category: "vegetable", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup", lowFodmapServing: "1 cup", notes: "All colors are low FODMAP" },
  { name: "Carrot", category: "vegetable", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 medium", lowFodmapServing: "1 medium", notes: "" },
  { name: "Spinach", category: "vegetable", fodmapLevel: "low", fodmapCategories: [], servingSize: "2 cups raw", lowFodmapServing: "2 cups raw", notes: "" },
  { name: "Tomato", category: "vegetable", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 medium", lowFodmapServing: "1 medium", notes: "Cherry tomatoes are moderate in larger amounts" },
  { name: "Zucchini", category: "vegetable", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup", lowFodmapServing: "1/2 cup", notes: "" },
  { name: "Mushrooms", category: "vegetable", fodmapLevel: "high", fodmapCategories: ["mannitol"], servingSize: "1 cup", lowFodmapServing: "Avoid or 2-3 pieces", notes: "Most varieties are high FODMAP" },
  { name: "Asparagus", category: "vegetable", fodmapLevel: "moderate", fodmapCategories: ["fructans"], servingSize: "5 spears", lowFodmapServing: "1 spear", notes: "" },
  { name: "Sweet Potato", category: "vegetable", fodmapLevel: "moderate", fodmapCategories: ["mannitol"], servingSize: "1 cup", lowFodmapServing: "1/2 cup", notes: "" },
  { name: "Potato", category: "vegetable", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 medium", lowFodmapServing: "1 medium", notes: "All varieties are low FODMAP" },
  { name: "Celery", category: "vegetable", fodmapLevel: "moderate", fodmapCategories: ["mannitol"], servingSize: "1 stalk", lowFodmapServing: "1/4 stalk", notes: "" },
  { name: "Cucumber", category: "vegetable", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup", lowFodmapServing: "1 cup", notes: "" },
  { name: "Green Beans", category: "vegetable", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup", lowFodmapServing: "3/4 cup", notes: "" },
  { name: "Cabbage (common)", category: "vegetable", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup", lowFodmapServing: "3/4 cup", notes: "Savoy cabbage is higher" },
  { name: "Eggplant", category: "vegetable", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup", lowFodmapServing: "1 cup", notes: "" },

  // ---- GRAINS ----
  { name: "White Rice", category: "grain", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup cooked", lowFodmapServing: "1 cup cooked", notes: "Safest grain for IBS" },
  { name: "Brown Rice", category: "grain", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup cooked", lowFodmapServing: "1 cup cooked", notes: "" },
  { name: "Oats", category: "grain", fodmapLevel: "low", fodmapCategories: [], servingSize: "1/2 cup dry", lowFodmapServing: "1/2 cup dry", notes: "Gluten-free oats recommended" },
  { name: "Quinoa", category: "grain", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup cooked", lowFodmapServing: "1 cup cooked", notes: "" },
  { name: "Wheat Bread", category: "grain", fodmapLevel: "high", fodmapCategories: ["fructans"], servingSize: "2 slices", lowFodmapServing: "1 slice sourdough spelt", notes: "Sourdough process reduces fructans" },
  { name: "Wheat Pasta", category: "grain", fodmapLevel: "high", fodmapCategories: ["fructans"], servingSize: "1 cup cooked", lowFodmapServing: "1/2 cup cooked or use gluten-free", notes: "" },
  { name: "Sourdough Bread (spelt)", category: "grain", fodmapLevel: "low", fodmapCategories: [], servingSize: "2 slices", lowFodmapServing: "2 slices", notes: "Fermentation reduces fructans" },
  { name: "Corn/Polenta", category: "grain", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup", lowFodmapServing: "1 cup", notes: "" },

  // ---- DAIRY ----
  { name: "Milk (cow)", category: "dairy", fodmapLevel: "high", fodmapCategories: ["lactose"], servingSize: "1 cup", lowFodmapServing: "Use lactose-free", notes: "" },
  { name: "Lactose-Free Milk", category: "dairy", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup", lowFodmapServing: "1 cup", notes: "" },
  { name: "Hard Cheese (cheddar, parmesan)", category: "dairy", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 oz", lowFodmapServing: "2 oz", notes: "Aging removes lactose" },
  { name: "Soft Cheese (ricotta, cottage)", category: "dairy", fodmapLevel: "high", fodmapCategories: ["lactose"], servingSize: "1/2 cup", lowFodmapServing: "2 tablespoons", notes: "" },
  { name: "Yogurt (regular)", category: "dairy", fodmapLevel: "high", fodmapCategories: ["lactose"], servingSize: "1 cup", lowFodmapServing: "Use lactose-free yogurt", notes: "" },
  { name: "Butter", category: "dairy", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 tablespoon", lowFodmapServing: "1 tablespoon", notes: "Minimal lactose" },
  { name: "Ice Cream", category: "dairy", fodmapLevel: "high", fodmapCategories: ["lactose"], servingSize: "1/2 cup", lowFodmapServing: "Use lactose-free or sorbet", notes: "" },

  // ---- PROTEIN ----
  { name: "Chicken", category: "protein", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 palm size", lowFodmapServing: "No limit", notes: "Plain, unprocessed" },
  { name: "Fish", category: "protein", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 fillet", lowFodmapServing: "No limit", notes: "Plain, unprocessed" },
  { name: "Eggs", category: "protein", fodmapLevel: "low", fodmapCategories: [], servingSize: "2 eggs", lowFodmapServing: "No limit", notes: "" },
  { name: "Beef", category: "protein", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 palm size", lowFodmapServing: "No limit", notes: "Plain, unprocessed" },
  { name: "Tofu (firm)", category: "protein", fodmapLevel: "low", fodmapCategories: [], servingSize: "1/2 cup", lowFodmapServing: "2/3 cup", notes: "Firm/extra-firm only. Silken tofu is high" },

  // ---- LEGUMES ----
  { name: "Lentils", category: "legume", fodmapLevel: "high", fodmapCategories: ["GOS", "fructans"], servingSize: "1 cup", lowFodmapServing: "1/4 cup canned, drained", notes: "Canning reduces FODMAPs" },
  { name: "Chickpeas", category: "legume", fodmapLevel: "high", fodmapCategories: ["GOS", "fructans"], servingSize: "1 cup", lowFodmapServing: "1/4 cup canned, drained", notes: "Canning reduces FODMAPs" },
  { name: "Black Beans", category: "legume", fodmapLevel: "high", fodmapCategories: ["GOS"], servingSize: "1 cup", lowFodmapServing: "1/4 cup canned, drained", notes: "" },
  { name: "Edamame", category: "legume", fodmapLevel: "moderate", fodmapCategories: ["GOS"], servingSize: "1 cup", lowFodmapServing: "1/2 cup", notes: "" },

  // ---- NUTS & SEEDS ----
  { name: "Almonds", category: "nuts", fodmapLevel: "low", fodmapCategories: [], servingSize: "10 almonds", lowFodmapServing: "10 almonds", notes: "Larger portions are moderate" },
  { name: "Cashews", category: "nuts", fodmapLevel: "high", fodmapCategories: ["GOS"], servingSize: "1/4 cup", lowFodmapServing: "Avoid or 10 nuts", notes: "" },
  { name: "Walnuts", category: "nuts", fodmapLevel: "low", fodmapCategories: [], servingSize: "10 halves", lowFodmapServing: "10 halves", notes: "" },
  { name: "Pistachios", category: "nuts", fodmapLevel: "high", fodmapCategories: ["fructans", "GOS"], servingSize: "1/4 cup", lowFodmapServing: "Avoid", notes: "" },
  { name: "Peanuts", category: "nuts", fodmapLevel: "low", fodmapCategories: [], servingSize: "32 nuts", lowFodmapServing: "32 nuts", notes: "" },
  { name: "Pumpkin Seeds", category: "nuts", fodmapLevel: "low", fodmapCategories: [], servingSize: "2 tablespoons", lowFodmapServing: "2 tablespoons", notes: "" },
  { name: "Chia Seeds", category: "nuts", fodmapLevel: "low", fodmapCategories: [], servingSize: "2 tablespoons", lowFodmapServing: "2 tablespoons", notes: "High in soluble fiber" },

  // ---- SWEETENERS ----
  { name: "Honey", category: "sweetener", fodmapLevel: "high", fodmapCategories: ["fructose"], servingSize: "1 tablespoon", lowFodmapServing: "Avoid. Use maple syrup", notes: "Very high in excess fructose" },
  { name: "Maple Syrup", category: "sweetener", fodmapLevel: "low", fodmapCategories: [], servingSize: "2 tablespoons", lowFodmapServing: "2 tablespoons", notes: "Best sweetener for IBS" },
  { name: "Table Sugar", category: "sweetener", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 tablespoon", lowFodmapServing: "1 tablespoon", notes: "Sucrose is low FODMAP in normal amounts" },
  { name: "High Fructose Corn Syrup", category: "sweetener", fodmapLevel: "high", fodmapCategories: ["fructose"], servingSize: "any", lowFodmapServing: "Avoid", notes: "Check labels on processed foods" },
  { name: "Sorbitol (artificial sweetener)", category: "sweetener", fodmapLevel: "high", fodmapCategories: ["sorbitol"], servingSize: "any", lowFodmapServing: "Avoid", notes: "Found in sugar-free gum and candy" },

  // ---- BEVERAGES ----
  { name: "Coffee (black)", category: "beverage", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup", lowFodmapServing: "1 cup", notes: "Can still irritate gut via caffeine" },
  { name: "Tea (black/green)", category: "beverage", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup", lowFodmapServing: "1 cup", notes: "Weak brew. Strong brew may be moderate" },
  { name: "Chamomile Tea", category: "beverage", fodmapLevel: "high", fodmapCategories: ["fructans"], servingSize: "1 cup strong", lowFodmapServing: "1 cup weak", notes: "" },
  { name: "Almond Milk", category: "beverage", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 cup", lowFodmapServing: "1 cup", notes: "Check for added high-FODMAP sweeteners" },
  { name: "Oat Milk", category: "beverage", fodmapLevel: "moderate", fodmapCategories: ["GOS"], servingSize: "1 cup", lowFodmapServing: "1/2 cup", notes: "" },
  { name: "Coconut Water", category: "beverage", fodmapLevel: "moderate", fodmapCategories: ["sorbitol"], servingSize: "1 cup", lowFodmapServing: "1/2 cup", notes: "" },

  // ---- CONDIMENTS ----
  { name: "Soy Sauce", category: "condiment", fodmapLevel: "low", fodmapCategories: [], servingSize: "2 tablespoons", lowFodmapServing: "2 tablespoons", notes: "" },
  { name: "Olive Oil", category: "condiment", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 tablespoon", lowFodmapServing: "1 tablespoon", notes: "All oils are FODMAP-free" },
  { name: "Garlic-Infused Oil", category: "condiment", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 tablespoon", lowFodmapServing: "1 tablespoon", notes: "Fructans are water-soluble, not oil-soluble. Safe substitute for garlic" },
  { name: "Ketchup", category: "condiment", fodmapLevel: "low", fodmapCategories: [], servingSize: "1 tablespoon", lowFodmapServing: "1 sachet", notes: "Check for HFCS" },
  { name: "Mayonnaise", category: "condiment", fodmapLevel: "low", fodmapCategories: [], servingSize: "2 tablespoons", lowFodmapServing: "2 tablespoons", notes: "" },
];

async function seed() {
  console.log(`Seeding ${fodmapFoods.length} foods to fodmapDatabase collection...`);

  const batch = db.batch();
  for (const food of fodmapFoods) {
    const id = food.name.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/-+$/, "");
    const ref = db.collection("fodmapDatabase").doc(id);
    batch.set(ref, food);
  }

  await batch.commit();
  console.log("Done! FODMAP database seeded successfully.");
  process.exit(0);
}

seed().catch((err) => {
  console.error("Seed failed:", err);
  process.exit(1);
});
