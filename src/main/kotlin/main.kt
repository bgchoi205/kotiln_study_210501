import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val articleRepository = ArticleRepository()
val memberRepository = MemberRepository()

var loginedMember : Member? = null

fun main(){

    val articleController = ArticleController()
    val memberController = MemberController()


    println("==프로그램 시작==")

    articleRepository.makeTestArticles()
    memberRepository.makeTestMember()

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
            "/member/join" -> {
                memberController.join()
            }
            "/member/login" -> {
                memberController.login()
            }


            "/member/list" -> {
                for(member in memberRepository.members){
                    println("번호 : ${member.id} / 아이디 : ${member.loginId} / 이름 : ${member.name} / 별명 : ${member.nickName}")
                }
            }
        }


    }

    println("==프로그램 끝==")
}

// Member 시작
// Member DTo
data class Member(
    val id : Int,
    val loginId : String,
    val loginPw : String,
    val name : String,
    val nickName : String
)


// MemberRepository 시작
class MemberRepository{

    val members = mutableListOf<Member>()
    var lastMemberId = 0

    fun joinMember(loginId: String, loginPw: String, name: String, nickName: String): Int {
        val id = ++lastMemberId
        members.add(Member(id, loginId, loginPw, name, nickName))
        return id
    }

    fun makeTestMember(){
        for(i in 1..20){
            joinMember("user$i", "user$i", "홍길동$i","사용자$i")
        }
    }

    fun getMemberByLoginId(loginId: String): Member? {
        for(member in members){
            if(member.loginId == loginId){
                return member
            }
        }
        return null
    }

}

// MemberRepository 끝


// MemberController 시작
class MemberController{
    fun join() {
        print("사용할 아이디 입력 : ")
        val loginId = readLineTrim()
        val member = memberRepository.getMemberByLoginId(loginId)
        if(member != null){
            println("사용중인 아이디입니다.")
            return
        }
        print("사용할 비밀번호 입력 : ")
        val loginPw = readLineTrim()
        print("이름 입력 : ")
        val name = readLineTrim()
        print("별명 입력 : ")
        val nickName = readLineTrim()

        val id = memberRepository.joinMember(loginId, loginPw, name, nickName)

        println("$id 번 회원으로 가입완료")
    }

    fun login() {
        print("아이디 입력 : ")
        val loginId = readLineTrim()
        val member = memberRepository.getMemberByLoginId(loginId)
        if(member == null){
            println("존재하지 않는 아이디입니다.")
            return
        }
        print("비밀번호 입력 : ")
        val loginPw = readLineTrim()
        if(member.loginPw != loginPw){
            println("비밀번호가 틀립니다.")
            return
        }
        loginedMember = member
        println("${member.nickName}님 환영합니다.")
    }

}

// MemberController 끝

// Member 끝


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