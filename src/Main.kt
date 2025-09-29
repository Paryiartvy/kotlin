import java.awt.Canvas

interface HaveId{
    val id: String
}

interface CanvasUnit{
    fun drawCanvas(): String
}

class Storage<T: HaveId>(){
//    просто хранилище с матодами добавления, удаления, считывания
    private var data: MutableList<T> = mutableListOf()

    fun add(item: T) = data.add(item)
    fun remove(item: T) = data.remove(item)
    fun getAll(): List<T> = data
    fun valueById(id: String) = data.find { it.id == id }
}

data class TextNoteModel( //класс базовой инфы о заметкие. требует поле id, остальные опциональны (имя, текст, статус)
    override var id: String,
    var name: String = "",
    var txt: String = "",
    var status: Boolean = false
): HaveId

open class Note<Data: HaveId>(initialData: Data): CanvasUnit{ //базовый класс заметки. хранит историю изменений заметки, поддерживает обновление содержимого, отображение, удаление
    private var localStorage: Storage<Data> = Storage()
    var data: Data = initialData
        set(value){
            field = value
            localStorage.add(value)
        }
        get() = field

    override fun drawCanvas(): String {
        return localStorage.getAll().joinToString("\n") { it.toString() }
    }

    fun update(data: Data) {
        this.data = data
    }
    fun willRemove(){
        localStorage = Storage()
    }
}

class TextNote(noteData: TextNoteModel): Note<TextNoteModel>(noteData){// экземпляр текстовой заметки
    override fun drawCanvas(): String{
        return "${data.name}: ${data.txt}"
    }
}

class ReminderNote(noteData: TextNoteModel): Note<TextNoteModel>(noteData){//экземпляр напоминалки
    override fun drawCanvas(): String {
        return "${data.txt}: ${if (data.status) "сделано" else "не сделано"}"
    }
}

class Notebook(): CanvasUnit{// класс для хранения списка заметок. можно добавлять, удалять и отображать все заметки
    private var notes: MutableList<Note<*>> = mutableListOf()

    fun add(note: Note<*>){
        notes.add(note)
    }
    fun remove(index: Int){
        notes.removeAt(index)
    }
    override fun drawCanvas(): String{
        return notes.joinToString("\n") {it.drawCanvas()}
    }
}

class ConsoleUI(){
    fun showMenuList(items: List<String>): Int{
        println("Выберите действие:")
        var k = 1
        for (i in items){
            println("$k) $i")
        k += 1
        }
        return readInt("Введите номер выбранной команды: ")
    }
    fun showCanvas(canvas: CanvasUnit){
        println(canvas.drawCanvas())
    }
    fun readString(userMessage: String = "Введите строковое значение"): String{
        println(userMessage)
        val str: String? = readLine()
        return str ?: ""
    }
    fun readInt(userMessage: String = "Введите целочисленное значение"): Int{
        println(userMessage)
        val tmp: Int? = readLine()?.toIntOrNull()
        return tmp ?: -1
    }
}

class Menu(){
    val ui = ConsoleUI()
    var notebook = Notebook()
}