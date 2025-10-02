import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface HaveId{
    val id: String
}

interface CanvasUnit{
    fun drawCanvas(): String
}

data class SimpleDateFormatter(var date: LocalDateTime) {// принимает дату, может вернуть ее в строковом виде
private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    fun format(): String = date.format(formatter)
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

open class Note<Data: HaveId>(initialData: Data, date: String): CanvasUnit{ //базовый класс заметки. хранит историю изменений заметки, поддерживает обновление содержимого, отображение, удаление
    private var localStorage: Storage<Data> = Storage()
    var createDate: String = date
        private set
    var updateDate: String = ""
        private set
    var data: Data = initialData
        set(value){
            field = value
            localStorage.add(value)
        }
        get() = field

    override fun drawCanvas(): String {
        return localStorage.getAll().joinToString("\n") { it.toString() }
    }

    fun update(data: Data, date: String) {
        this.data = data
        updateDate = date
    }
    fun willRemove(){
        localStorage = Storage()
    }
}

class TextNote(noteData: TextNoteModel, date: String): Note<TextNoteModel>(noteData, date){// экземпляр текстовой заметки
    override fun drawCanvas(): String{
        return """id:${data.id}
                ${data.name}: ${data.txt}
                Создано:${createDate}
                Изменено:${updateDate}""".trimIndent()
    }
}

class ReminderNote(noteData: TextNoteModel, date: String): Note<TextNoteModel>(noteData, date){//экземпляр напоминалки
    override fun drawCanvas(): String {
        return """id:${data.id}
                ${data.txt}: ${if (data.status) "сделано" else "не сделано"}
                Создано:${createDate}
                Изменено:${updateDate}""".trimIndent()
    }
}

class Notebook(): CanvasUnit{// класс для хранения списка заметок. можно добавлять, удалять и отображать все заметки
    private var notes: MutableList<Note<*>> = mutableListOf()
    private var lastId = 0

    fun getLastId() = lastId
    fun add(note: Note<*>){
        notes.add(note)
        lastId += 1
    }
    fun remove(index: Int){
        notes.removeAt(index)
    }
    fun removeById(id: String) {
        val note = notes.find { it.data.id == id }
        if (note != null) {
            note.willRemove()
            notes.remove(note)
        }
    }

    fun getById(id: String): Note<*>?{
        val note = notes.find { it.data.id == id }
        if (note != null) {
            return note
        }
        else
            return null
    }

    fun existsId(id: String): Boolean {
        return notes.any { it.data.id == id }
    }
    override fun drawCanvas(): String{
        return notes.joinToString("\n") {it.drawCanvas()}
    }
}

class ConsoleUI(){//выбор пунктов меню, отображение элемента, считывание строки/числа
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

class Menu(){//основа приложение. бесконечный цикл считывание команд, возможность добавления/изменения/удаления заметок
    private val ui = ConsoleUI()
    private val notebook = Notebook()

    fun choice(){
        var fl = true
        while (fl){
            val userInput =
                ui.showMenuList(listOf("Просмотр всех заметок", "Добавить заметку", "Редактировать заметку", "Удалить заметку", "Выход"))
            when (userInput){
                1 -> ui.showCanvas(notebook)
                2 -> addNote()
                3 -> editNote()
                4 -> deleteNote()
                5 -> fl = false
                else -> println("Некорректный ввод")
            }
        }
    }

    fun addNote(){
        var fl = true
        while (fl){
            val userInput =
                ui.showMenuList(listOf("Добавить текстовую заметку", "Добавить заметку-напоминание", "Выход"))
            when (userInput){
                1 -> {addTextNote(); fl = false}
                2 -> {addReminderNote(); fl = false}
                3 -> fl = false
                else -> println("Некорректный ввод")
            }
        }
    }
    fun addTextNote(){
        val name =
            ui.readString("Введите имя заметки:")
        val txt =
            ui.readString("Введите текст заметки:")
        notebook.add(TextNote(
            TextNoteModel(notebook.getLastId().toString(), name, txt),
            SimpleDateFormatter(LocalDateTime.now()).format()))
    }
    fun addReminderNote(){
        val txt =
            ui.readString("Введите текст заметки:")
        notebook.add(ReminderNote(
            TextNoteModel(notebook.getLastId().toString(),"", txt),
            SimpleDateFormatter(LocalDateTime.now()).format()))
    }

    fun editNote(){
        var fl = true
        while (fl){
            val userInput =
                ui.readString("Введите id заметки, которую хотите изменить")
            if (notebook.existsId(userInput)){
                val tmp = notebook.getById(userInput)
                tmp?.let{
                    when (it) {
                        is TextNote -> {
                            val newName = ui.readString("Введите новое имя заметки:")
                            val newText = ui.readString("Введите новый текст заметки:")
                            it.update(TextNoteModel(it.data.id, newName, newText),
                                SimpleDateFormatter(LocalDateTime.now()).format()
                            )
                        }
                        is ReminderNote -> {
                            val newText = ui.readString("Введите новый текст напоминания:")
                            val newStatus = ui.readInt("Введите статус (1 — сделано, 0 — не сделано):")
                            it.update(
                                TextNoteModel(
                                    it.data.id,
                                    "",
                                    newText,
                                    status = (newStatus == 1)
                                ),
                                SimpleDateFormatter(LocalDateTime.now()).format()
                            )
                        }
                    }
                }
                println("Заметка успешно изменена")
                fl = false
            }
            else {
                println("Id не найден")
            }
        }
    }

    fun deleteNote(){
        var fl = true
        while (fl){
            val userInput =
                ui.readString("Введите id заметки, которую хотите удалить")
            if (notebook.existsId(userInput)){
                notebook.removeById(userInput)
                println("Заметка успешно удалена")
                fl = false
            }
            else {
                println("Id не найден")
            }
        }
    }
}

class NoteApp{//создает экземпляр приложения
    private val menu = Menu()
    fun start() = menu.choice()
}


fun main(){
    val  app = NoteApp()
    app.start()
}