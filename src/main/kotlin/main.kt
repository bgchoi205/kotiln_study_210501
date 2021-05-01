import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val articleRepository = ArticleRepository()

fun main(){

    val articleController = ArticleController()


    println("==프로그램 시작==")

    articleRepository.makeTestArticles()

    while(true){

        print("명령어 입력 : ")
        val cmd = readLineTrim()

        val rq = Rq(cmd)

        when(rq.actionPath){

            "/exit" -> {
                println("종료합니다.")
                break
            }
            "/article/write" -> {
                articleController.write()
            }
            "/article/list" -> {
                articleController.list(rq)
            }
            "/article/detail" -> {
                articleController.detail(rq)
            }
            "/article/delete" -> {
                articleController.delete(rq)
            }
            "/article/modify" -> {
                articleController.modify(rq)
            }


        }


    }

    println("==프로그램 끝==")
}


// Article 시작
// Article DTO
data class Article(
    val id : Int,
    var title : String,
    var body : String,
    val regDate : String,
    var updateDate : String
)

// ArticleRepository 시작
class ArticleRepository{

    val articles = mutableListOf<Article>()
    var lastId = 0

    fun addArticle(title : String, body : String) : Int{
        val id = ++lastId
        val regDate = Util.getDateNowStr()
        val updateDate = Util.getDateNowStr()
        articles.add(Article(id, title, body, regDate, updateDate))
        return id
    }

    fun makeTestArticles(){
        for(i in 1..30) {
            addArticle("제목$i", "내용$i")
        }
    }

    fun getArticleById(id : Int): Article? {
        for(article in articles){
            if(article.id == id){
                return article
            }
        }
        return null
    }

    fun articlesFilter(keyword: String, page: Int, pageCount: Int): List<Article> {
        val filteredArticles = articlesFilterByKey(keyword)
        var filteredArtciles2 = mutableListOf<Article>()

        val startIndex = filteredArticles.lastIndex - ((page - 1) * pageCount)
        var endIndex = startIndex - pageCount + 1
        if(endIndex < 0){
            endIndex = 0
        }
        if(filteredArticles.isEmpty()){
            filteredArtciles2 = articles
        }
        for(i in startIndex downTo endIndex){
            filteredArtciles2.add(filteredArticles[i])
        }
        return filteredArtciles2
    }

    private fun articlesFilterByKey(keyword: String): List<Article> {
        val filteredArticlesByKey = mutableListOf<Article>()
        for(article in articles){
            if(article.title.contains(keyword)){
                filteredArticlesByKey.add(article)
            }
        }
        return filteredArticlesByKey
    }
}

// ArticleRepository 끝


// ArticleController 시작
class ArticleController{
    fun write(){
        print("제목 입력 : ")
        val title = readLineTrim()
        print("내용 입력 : ")
        val body = readLineTrim()
        val id = articleRepository.addArticle(title, body)
        println("$id 번 게시물이 등록되었습니다.")
    }

    fun list(rq: Rq) {
        val keyword = rq.getStringParam("keyword","")
        val page = rq.getIntParam("page",1)

        val filteredArticles = articleRepository.articlesFilter(keyword, page, 5)
        for(article in filteredArticles){
            println("번호 : ${article.id} / 제목 : ${article.title} / 등록날짜 : ${article.regDate}")
        }
    }

    fun detail(rq: Rq) {
        val id = rq.getIntParam("id", 0)
        if(id == 0){
            println("id를 입력해주세요.")
            return
        }
        val article = articleRepository.getArticleById(id)
        if(article == null){
            println("없는 게시물 번호입니다.")
            return
        }
        println("번호 : ${article.id}")
        println("제목 : ${article.title}")
        println("내용 : ${article.body}")
        println("등록일 : ${article.regDate}")
        println("수정일 : ${article.updateDate}")
    }

    fun delete(rq: Rq) {
        val id = rq.getIntParam("id", 0)
        if(id == 0){
            println("id를 입력해주세요.")
            return
        }
        val article = articleRepository.getArticleById(id)
        if(article == null){
            println("없는 게시물 번호입니다.")
            return
        }
        articleRepository.articles.remove(article)
        println("$id 번 게시물 삭제완료")
    }

    fun modify(rq: Rq) {
        val id = rq.getIntParam("id", 0)
        if(id == 0){
            println("id를 입력해주세요.")
            return
        }
        val article = articleRepository.getArticleById(id)
        if(article == null){
            println("없는 게시물 번호입니다.")
            return
        }
        print("새 제목 입력 : ")
        val title = readLineTrim()
        print("새 내용 입력 : ")
        val body = readLineTrim()
        article.title = title
        article.body = body
        article.updateDate = Util.getDateNowStr()
        println("$id 번 게시물 수정 완료")
    }
}

// ArticleController 끝

// Article 끝



fun readLineTrim() = readLine()!!.trim()

object Util{
    fun getDateNowStr() : String{
        var now = LocalDateTime.now()
        var getNowStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH시 mm분 ss초"))
        return getNowStr
    }
}