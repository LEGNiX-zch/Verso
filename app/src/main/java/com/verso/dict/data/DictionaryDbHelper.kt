package com.verso.dict.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Local offline dictionary database. Pre-populated on first launch with a built-in
 * English-Chinese word set so the app works fully offline without any network access.
 */
class DictionaryDbHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${Table.WORDS} (
                ${Table.COL_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${Table.COL_WORD} TEXT NOT NULL,
                ${Table.COL_DEFINITION} TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_word ON ${Table.WORDS}(${Table.COL_WORD})")
        db.execSQL("CREATE INDEX idx_definition ON ${Table.WORDS}(${Table.COL_DEFINITION})")
        seedData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${Table.WORDS}")
        onCreate(db)
    }

    private fun seedData(db: SQLiteDatabase) {
        val rows = listOf(
            "apple" to "苹果；一种水果",
            "banana" to "香蕉；长条状热带水果",
            "orange" to "橙子；橘子；橙色的",
            "grape" to "葡萄；成串的水果",
            "water" to "水；饮用水；浇花",
            "fire" to "火；开火；点燃",
            "book" to "书；书籍；预订",
            "pen" to "钢笔；笔；写字",
            "desk" to "书桌；桌子；办公桌",
            "chair" to "椅子；座位；担任主席",
            "school" to "学校；学院；上学",
            "teacher" to "老师；教师；教书的人",
            "student" to "学生；学习者",
            "friend" to "朋友；友人；同伴",
            "family" to "家庭；家人；家族",
            "father" to "父亲；爸爸",
            "mother" to "母亲；妈妈",
            "brother" to "兄弟；哥哥或弟弟",
            "sister" to "姐妹；姐姐或妹妹",
            "home" to "家；家庭；回家",
            "house" to "房子；住宅；居住",
            "room" to "房间；屋子；空间",
            "door" to "门；门口；通道",
            "window" to "窗户；窗子；窗口",
            "tree" to "树；树木；树状物",
            "flower" to "花；花朵；开花",
            "grass" to "草；青草；草地",
            "river" to "河；河流；江河",
            "mountain" to "山；山脉；高山",
            "sea" to "海；海洋；大海",
            "sun" to "太阳；阳光；日",
            "moon" to "月亮；月球；月光",
            "star" to "星星；恒星；明星",
            "sky" to "天空；天；苍穹",
            "rain" to "雨；下雨；雨水",
            "snow" to "雪；下雪；雪花",
            "wind" to "风；刮风；气流",
            "cloud" to "云；云朵；云端",
            "dog" to "狗；犬；犬类动物",
            "cat" to "猫；猫咪；猫科动物",
            "bird" to "鸟；鸟类；飞禽",
            "fish" to "鱼；鱼肉；捕鱼",
            "horse" to "马；骑马；马匹",
            "cow" to "牛；奶牛；母牛",
            "rice" to "米饭；稻米；大米",
            "bread" to "面包；面包类食物",
            "milk" to "牛奶；奶；哺乳",
            "tea" to "茶；茶叶；茶水",
            "coffee" to "咖啡；咖啡饮料",
            "sugar" to "糖；白糖；加糖",
            "salt" to "盐；食盐；加盐",
            "car" to "汽车；小轿车；车厢",
            "bus" to "公共汽车；巴士；乘公交",
            "train" to "火车；列车；训练",
            "plane" to "飞机；航班；平面",
            "road" to "路；道路；公路",
            "city" to "城市；都市；市区",
            "country" to "国家；乡村；乡村的",
            "world" to "世界；天地；世间",
            "time" to "时间；时刻；年代",
            "day" to "天；日子；白天",
            "night" to "夜晚；夜里；黑夜",
            "year" to "年；年份；岁数",
            "month" to "月；月份；一个月",
            "week" to "周；星期；一周",
            "hour" to "小时；钟头；时间",
            "minute" to "分钟；分；片刻",
            "money" to "钱；金钱；财富",
            "price" to "价格；价钱；定价",
            "market" to "市场；集市；行情",
            "shop" to "商店；店铺；购物",
            "food" to "食物；食品；粮食",
            "color" to "颜色；色彩；着色",
            "red" to "红色；红色的",
            "blue" to "蓝色；蓝色的",
            "green" to "绿色；绿色的",
            "yellow" to "黄色；黄色的",
            "black" to "黑色；黑色的",
            "white" to "白色；白色的",
            "happy" to "快乐的；幸福的；高兴",
            "sad" to "悲伤的；难过的；哀愁",
            "good" to "好的；优良的；善良",
            "bad" to "坏的；糟糕的；劣质",
            "big" to "大的；巨大的；重要",
            "small" to "小的；微小的；不重要",
            "fast" to "快的；迅速的；快",
            "slow" to "慢的；缓慢的；迟缓",
            "new" to "新的；崭新的；新鲜",
            "old" to "旧的；老的；古老",
            "hot" to "热的；炎热的；辣",
            "cold" to "冷的；寒冷的；感冒",
            "love" to "爱；喜爱；爱情",
            "work" to "工作；劳动；职业",
            "play" to "玩；游戏；演奏",
            "read" to "读；阅读；看书",
            "write" to "写；书写；写作",
            "run" to "跑；奔跑；运行",
            "walk" to "走；步行；散步",
            "eat" to "吃；进食；用餐",
            "drink" to "喝；饮；饮料",
            "sleep" to "睡觉；睡眠；入睡",
            "open" to "打开；开放；开启",
            "close" to "关闭；合上；靠近",
            "buy" to "买；购买；交易",
            "sell" to "卖；出售；销售",
            "help" to "帮助；帮忙；援助",
            "learn" to "学习；学会；得知",
            "think" to "想；思考；认为",
            "speak" to "说；讲话；发言"
        )
        val cv = ContentValues()
        db.beginTransaction()
        try {
            for ((word, def) in rows) {
                cv.clear()
                cv.put(Table.COL_WORD, word)
                cv.put(Table.COL_DEFINITION, def)
                db.insert(Table.TABLE_WORDS, null, cv)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** Forward search: English word prefix match. */
    fun searchByWord(keyword: String): List<Word> {
        val results = ArrayList<Word>()
        if (keyword.isBlank()) return results
        val key = "${keyword.trim().lowercase()}%"
        val db = readableDatabase
        val c = db.query(
            Table.WORDS,
            null,
            "${Table.COL_WORD} LIKE ? COLLATE NOCASE",
            arrayOf(key),
            null,
            null,
            "${Table.COL_WORD} ASC"
        )
        c.use {
            while (it.moveToNext()) {
                results.add(
                    Word(
                        it.getLong(it.getColumnIndexOrThrow(Table.COL_ID)),
                        it.getString(it.getColumnIndexOrThrow(Table.COL_WORD)),
                        it.getString(it.getColumnIndexOrThrow(Table.COL_DEFINITION))
                    )
                )
            }
        }
        return results
    }

    /** Reverse search: match English words whose Chinese definition contains the keyword. */
    fun searchByDefinition(keyword: String): List<Word> {
        val results = ArrayList<Word>()
        if (keyword.isBlank()) return results
        val key = "%${keyword.trim()}%"
        val db = readableDatabase
        val c = db.query(
            Table.WORDS,
            null,
            "${Table.COL_DEFINITION} LIKE ?",
            arrayOf(key),
            null,
            null,
            "${Table.COL_WORD} ASC"
        )
        c.use {
            while (it.moveToNext()) {
                results.add(
                    Word(
                        it.getLong(it.getColumnIndexOrThrow(Table.COL_ID)),
                        it.getString(it.getColumnIndexOrThrow(Table.COL_WORD)),
                        it.getString(it.getColumnIndexOrThrow(Table.COL_DEFINITION))
                    )
                )
            }
        }
        return results
    }

    object Table {
        const val WORDS = "words"
        const val TABLE_WORDS = "words"
        const val COL_ID = "_id"
        const val COL_WORD = "word"
        const val COL_DEFINITION = "definition"
    }

    companion object {
        private const val DB_NAME = "verso_dict.db"
        private const val DB_VERSION = 1
    }
}
