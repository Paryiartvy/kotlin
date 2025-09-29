interface HaveId{
    val id: String
}

interface CanvasUnit{
    fun drawCanvas(): String
}

class Storage<T: HaveId>(){
    private var data: MutableList<T> = mutableListOf()

    fun add(item: T) = data.add(item)
    fun remove(item: T) = data.remove(item)
    fun getAll(): List<T> = data
    fun valueById(id: String) = data.find { it.id == id }
}

data class TextNoteModel(
    override var id: String,
    var name: String,
    var txt: String = "",
    var status: Boolean = false
): HaveId

open class Note<Data: HaveId>(initialData: Data): CanvasUnit{
    private var localStorage: Storage<Data> = Storage()
    var data: Data = initialData
        set(value){
            field = value
            localStorage.add(value)
        }
        get() = field

    override fun drawCanvas(): String{
        return localStorage.toString()
    }
    fun update(data: Data) {
        this.data = data
    }
    fun willRemove(){
        localStorage = Storage()
    }
}

class TextNote(noteData: TextNoteModel): Note<TextNoteModel>(noteData){
    override fun drawCanvas(): String{
        return "${data.name}: ${data.txt}"
    }
}

class ReminderNote(noteData: TextNoteModel): Note<TextNoteModel>(noteData){
    override fun drawCanvas(): String {
        return "${data.txt}: ${if (data.status) "сделано" else "не сделано"}"
    }
}