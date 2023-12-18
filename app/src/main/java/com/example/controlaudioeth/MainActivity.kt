package com.example.controlaudioeth

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatDelegate
import com.example.controlaudioeth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnLongClickListener {
  private lateinit var binding: ActivityMainBinding
  private var buttons = arrayListOf<Button>()
  private val groupArray = arrayListOf<ArrayList<Boolean>>()
  private val memoryArray = arrayListOf<ArrayList<Boolean>>()
  private var isChanging = false
  private var isAllToAll = false
  private var isSolo = false
  private var isSettingGroups = arrayListOf(false, false, false, false, false)
  private var groupButtons = arrayListOf<ToggleButton>()
  private var counterButtons = arrayListOf<ToggleButton>()
  private var memoryIndicator = false
  private lateinit var filterArray : ArrayList<ToggleButton>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.hide()
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    filterArray = arrayListOf(binding.memoryButton, binding.micButton, binding.auxInButton)
    resetMemoryArray()
    clearGroupArray()
    val interval = (5000 / 2).toLong()

    counterButtons = arrayListOf(
      binding.counter1Button,
      binding.counter2Button,
      binding.counter3Button,
      binding.counter4Button,
      binding.counter5Button,
      binding.counter6Button,
      binding.counter7Button,
      binding.counter8Button
    )
    groupButtons = arrayListOf(
      binding.groupAButton,
      binding.groupBButton,
      binding.groupCButton,
      binding.groupDButton
    )
    getAllButtons(binding.mainActivityContainer)
    resetAllButtons()

    val runnable = object : Runnable {
      override fun run() {
        if (!binding.autoChangeButton.isChecked) return
        switchCounterButton(counterButtons)
        println(interval)
        if (binding.autoChangeButton.isChecked){
          binding.root.handler.postDelayed(this, interval)
        }
      }
    }

    for (i in counterButtons.indices) {
      counterButtons[i].setOnCheckedChangeListener { button: CompoundButton, isChecked ->
        for(j in groupButtons.indices) {
          if (isSettingGroups[j]){
            if (isChecked){
              if (isSettingGroups[4]){
                for (k in memoryArray.indices){
                  memoryArray[k][i] = (k == j)
                }
              } else {
                for (k in groupArray.indices) {
                  groupArray[k][i] = (k == j)
                }
              }
              colorButton(button, Colors.WHITE)
            } else {
              if (isSettingGroups[4]){
                memoryArray[j][i] = false
              } else if (isSettingGroups[j]){
                groupArray[j][i] = false
              }
              if (memoryArray[0][i] or memoryArray[1][i] or memoryArray[2][i] or memoryArray[3][i]) colorButton(counterButtons[i], Colors.LIGHTRED)
              else colorButton(button, Colors.GREEN)
            }
          }
        }
        if (!groupButtons.any { it.isChecked }){
          if(binding.autoChangeButton.isChecked) {
            if (isChecked) {
              colorButton(button, Colors.WHITE)
              if(isChanging) {
                resetButtonsExcept(arrayListOf(binding.autoChangeButton, *filterArray.toTypedArray()))
              } else {
                resetButtonsExcept()
              }
              button.isChecked = true
            } else {
              colorButton(counterButtons[i], Colors.RESETDARK)
              if(isChanging) {
                resetButtonsExcept(arrayListOf(binding.autoChangeButton, *filterArray.toTypedArray()))
              } else {
                resetButtonsExcept()
              }
            }
          } else {
            if (!isSettingGroups.dropLast(1).any { it }){
              if (isChecked) {
                if (!isAllToAll && !isSolo) {
                  val memberArray = if(binding.memoryButton.isChecked) {
                    arrayListOf(memoryArray[0][i], memoryArray[1][i], memoryArray[2][i], memoryArray[3][i])
                  } else arrayListOf(groupArray[0][i], groupArray[1][i], groupArray[2][i], groupArray[3][i])
                  if (memberArray.any{ it }){
                    groupButtons[memberArray.indexOf(memberArray.find { it })].isChecked = true
                    checkGroup(memberArray.indexOf(memberArray.find { it }))

                    println("GROUP BUTTON: ${memberArray.indexOf(memberArray.find { it })} = ${groupButtons[memberArray.indexOf(true)].isChecked}")
                  } else {
                    resetButtonsExcept()
                    colorButton(button, Colors.WHITE)
                  }
                } else {
                  if (isAllToAll) {
                    val tempArr = arrayListOf<ToggleButton>()
                    tempArr.addAll(arrayListOf(binding.allToAllButton, *filterArray.toTypedArray()) + counterButtons)
                    resetButtonsExcept(tempArr)
                  }
                  if (isSolo) resetButtonsExcept(arrayListOf(binding.soloButton, *filterArray.toTypedArray()))
                }
                button.isChecked = true
              } else {
                colorButton(button, Colors.RESETDARK)
                if (isSolo) {
                  resetButtonsExcept(arrayListOf(binding.soloButton, *filterArray.toTypedArray()))
                } else {
                  resetButtonsExcept()
                }
              }
            }
          }
        } else {
          val memberArray = if(binding.memoryButton.isChecked) {
            arrayListOf(memoryArray[0][i], memoryArray[1][i], memoryArray[2][i], memoryArray[3][i])
          } else arrayListOf(groupArray[0][i], groupArray[1][i], groupArray[2][i], groupArray[3][i])
          if(!isSettingGroups.any { it }){
            if (memberArray.any { it }) {
              if (!groupButtons[memberArray.indexOf(memberArray.find { it })].isChecked) {
                groupButtons[memberArray.indexOf(memberArray.find { it })].isChecked = true
                checkGroup(memberArray.indexOf(memberArray.find { it }))
              }
            } else {
              for (groupButton in groupButtons) groupButton.isChecked = false
              resetButtonsExcept()
              colorButton(button, Colors.WHITE)
            }
          }
        }
      }
    }
    for (i in groupButtons.indices) {
      groupButtons[i].setOnLongClickListener( this )
      groupButtons[i].setOnCheckedChangeListener { _: CompoundButton?, isChecked ->
        if (!(isSettingGroups.any{ it })) {
          if (isChecked) {
            if (isSolo) {
              resetButtonsExcept(arrayListOf(binding.soloButton, *filterArray.toTypedArray()))
            } else {
              resetButtonsExcept()
            }
            groupButtons[i].isChecked = true
            if (binding.memoryButton.isChecked) {
              for (j in memoryArray[i].indices) {
                if (memoryArray[i][j]) {
                  colorButton(counterButtons[j], Colors.WHITE)
                  counterButtons[j].isChecked = true
                } else {
                  colorButton(counterButtons[j], Colors.RESETDARK)
                  counterButtons[j].isChecked = false
                }
              }
            } else {
              for (j in groupArray[i].indices) {
                if (groupArray[i][j]) {
                  colorButton(counterButtons[j], Colors.WHITE)
                  counterButtons[j].isChecked = true
                } else {
                  colorButton(counterButtons[j], Colors.RESETDARK)
                  counterButtons[j].isChecked = false
                }
              }
            }
            colorButton(groupButtons[i], Colors.ORANGE)
          } else {
            colorButton(groupButtons[i], Colors.RESETMID)
            if (isSolo) {
              resetButtonsExcept(arrayListOf(binding.soloButton, *filterArray.toTypedArray()))
            } else {
              resetButtonsExcept()
            }
            if (binding.memoryButton.isChecked) {
              if (memoryArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
              else colorButton(groupButtons[i], Colors.RESETMID)
            } else {
              if (groupArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
              else colorButton(groupButtons[i], Colors.RESETMID)
            }
          }
        } else {
          groupButtons[i].isChecked = false
          if (!memoryIndicator){
            Toast.makeText(this, "Najpierw wyjdź z trybu przypisywania!", Toast.LENGTH_SHORT).show()
          }
        }
      }
    }
    binding.memoryButton.setOnLongClickListener( this )
    binding.memoryButton.setOnCheckedChangeListener { button: CompoundButton, isChecked ->
      if (isChecked) {
        if (isSettingGroups[4]){
          button.isChecked = false
        } else {
          resetButtonsExcept()
          colorButton(button, Colors.BLUE)
          button.isChecked = true
          for (i in groupButtons.indices) {
            if (memoryArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
            else colorButton(groupButtons[i], Colors.RESETMID)
          }
        }
      } else {
        if (!isSettingGroups.dropLast(1).any { it }){
          resetCounters()
        }
        colorButton(button, Colors.RESETLIGHT)
        resetButtonsExcept()
        for (i in groupButtons.indices) {
          if (groupArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
          else colorButton(groupButtons[i], Colors.RESETMID)
        }
      }
    }
    binding.clearButton.setOnCheckedChangeListener { _: CompoundButton?, isChecked ->
      if(isChecked){
        if (binding.memoryButton.isChecked) resetMemoryArray()
        else clearGroupArray()
        resetButtonsExcept()
      } else {
        resetButtonsExcept()
      }
    }
    binding.muteAllButton.setOnCheckedChangeListener { _: CompoundButton?, isChecked ->
      if (!isSettingGroups.any { it }) {
        if (isChecked) {
          resetButtonsExcept()
          colorButton(binding.muteAllButton, Colors.RED)
          binding.muteAllButton.isChecked = true
        } else {
          colorButton(binding.muteAllButton, Colors.RESETLIGHT)
          resetButtonsExcept()
        }
      } else {
        binding.muteAllButton.isChecked = false
        Toast.makeText(this, "Najpierw wyjdź z trybu przypisywania!", Toast.LENGTH_SHORT).show()
      }
    }
    binding.speakerButton.setOnCheckedChangeListener { _: CompoundButton?, isChecked ->
      if (!isSettingGroups.any { it }) {
        if (isChecked) {
          colorButton(binding.speakerButton, Colors.ORANGE)
        } else {
          colorButton(binding.speakerButton, Colors.RESETLIGHT)
        }
      } else {
        binding.speakerButton.isChecked = false
        Toast.makeText(this, "Najpierw wyjdź z trybu przypisywania!", Toast.LENGTH_SHORT).show()
      }
    }
    binding.allToAllButton.setOnCheckedChangeListener { _: CompoundButton?, isChecked ->
      if (!isSettingGroups.any { it }) {
        if (isChecked) {
          resetButtonsExcept()
          colorButton(binding.allToAllButton, Colors.ORANGE)
          binding.allToAllButton.isChecked = true
          isAllToAll = true
          for (i in counterButtons) {
            colorButton(i, Colors.WHITE)
            i.isChecked = true
            i.isClickable = false
          }
        } else {
          colorButton(binding.allToAllButton, Colors.RESETLIGHT)
          resetButtonsExcept()
          isAllToAll = false
          for (i in counterButtons) {
            colorButton(i, Colors.RESETDARK)
            i.isChecked = false
            i.isClickable = true
          }
        }
      } else {
        binding.allToAllButton.isChecked = false
        Toast.makeText(this, "Najpierw wyjdź z trybu przypisywania!", Toast.LENGTH_SHORT).show()
      }
    }
    binding.auxInButton.setOnCheckedChangeListener { _: CompoundButton?, isChecked ->
      if(isChecked){
        colorButton(binding.auxInButton, Colors.ORANGE)
      } else {
        colorButton(binding.auxInButton, Colors.RESETLIGHT)
      }
    }
    binding.micButton.setOnCheckedChangeListener { _: CompoundButton?, isChecked ->
      if(isChecked){
        colorButton(binding.micButton, Colors.RED)
      } else {
        colorButton(binding.micButton, Colors.RESETLIGHT)
      }
    }
    binding.autoChangeButton.setOnCheckedChangeListener { _: CompoundButton?, isChecked ->
      if (!(isSettingGroups.any{ it })) {
        if (isChecked) {
          stopSetting()
          resetButtonsExcept()
          colorButton(counterButtons[0], Colors.WHITE)
          colorButton(binding.autoChangeButton, Colors.WHITE)
          counterButtons[0].isChecked = true
          binding.autoChangeButton.isChecked = true

          binding.root.handler.postDelayed(runnable, interval)
        } else {
          binding.root.handler.removeCallbacks(runnable)
          resetButtonsExcept()
          colorButton(binding.autoChangeButton, Colors.RESETMID)
        }
      } else {
        binding.autoChangeButton.isChecked = false
        Toast.makeText(this, "Najpierw wyjdź z trybu przypisywania!", Toast.LENGTH_SHORT).show()
      }
    }
    binding.soloButton.setOnCheckedChangeListener { _: CompoundButton?, isChecked ->
      if (!groupButtons.any { it.isChecked }) {

        val tempVal = counterButtons.find { it.isChecked }

        if (isChecked) {
          isSolo = true
          colorButton(binding.soloButton, Colors.ORANGE)
          binding.micButton.isChecked = true
          if (tempVal != null) {
            resetButtonsExcept(arrayListOf(tempVal, binding.soloButton, *filterArray.toTypedArray()))
          } else {
            resetButtonsExcept(arrayListOf(binding.soloButton, *filterArray.toTypedArray()))
          }
          binding.soloButton.isChecked = true
        } else {
          isSolo = false
          colorButton(binding.soloButton, Colors.RESETMID)
          resetButtonsExcept()
          binding.soloButton.isChecked = false
        }
      } else {
        val tempArr = arrayListOf<ToggleButton>()
        var tempVal: ToggleButton = binding.groupAButton
        when (groupButtons.find { it.isChecked }) {
          binding.groupAButton -> for (i in groupArray[0].indices) {
            tempVal = binding.groupAButton
            if (binding.memoryButton.isChecked){
              if (memoryArray[0][i]) {
                tempArr.add(counterButtons[i])
              }
            } else {
              if (groupArray[0][i]) {
                tempArr.add(counterButtons[i])
              }
            }
          }
          binding.groupBButton -> for (i in groupArray[0].indices) {
            tempVal = binding.groupBButton
            if (binding.memoryButton.isChecked){
              if (memoryArray[1][i]) {
                tempArr.add(counterButtons[i])
              }
            } else {
              if (groupArray[1][i]) {
                tempArr.add(counterButtons[i])
              }
            }
          }
          binding.groupCButton -> for (i in groupArray[0].indices) {
            tempVal = binding.groupCButton
            if (binding.memoryButton.isChecked){
              if (memoryArray[2][i]) {
                tempArr.add(counterButtons[i])
              }
            } else {
              if (groupArray[2][i]) {
                tempArr.add(counterButtons[i])
              }
            }
          }
          binding.groupDButton -> for (i in groupArray[0].indices) {
            tempVal = binding.groupDButton
            if (binding.memoryButton.isChecked){
              if (memoryArray[3][i]) {
                tempArr.add(counterButtons[i])
              }
            } else {
              if (groupArray[3][i]) {
                tempArr.add(counterButtons[i])
              }
            }
          }
        }
        if (isChecked) {
          isSolo = true
          colorButton(binding.soloButton, Colors.ORANGE)
          tempArr.addAll(arrayListOf(tempVal, binding.soloButton, *filterArray.toTypedArray()))
          resetButtonsExcept(tempArr)
          binding.soloButton.isChecked = true
        } else {
          isSolo = false
          colorButton(binding.soloButton, Colors.RESETMID)
          tempArr.addAll(arrayListOf(tempVal, *filterArray.toTypedArray()))
          resetButtonsExcept(tempArr)
          binding.soloButton.isChecked = false
        }
      }
    }
  }

  private fun resetButtonsExcept(filter: ArrayList<ToggleButton> = filterArray) {
    for(i in buttons) {
      if((!filter.contains(i)) && (i is ToggleButton)) {
        i.isChecked = false
      }
    }
  }

  private fun clearGroupArray(){
    groupArray.clear()
    for (i in 1..4) {
      groupArray.add(arrayListOf(false, false, false, false, false, false, false, false))
    }
  }

  private fun resetMemoryArray(){
    memoryArray.clear()
    memoryArray.add(arrayListOf(true, true, false, false, false, false, false, false))
    memoryArray.add(arrayListOf(false, false, true, true, false, false, false, false))
    memoryArray.add(arrayListOf(false, false, false, false, true, true, false, false))
    memoryArray.add(arrayListOf(false, false, false, false, false, false, true, true))
  }

  private fun resetAllButtons(){
    for(i in buttons) {
      if(i is ToggleButton) {
        i.isChecked = false
      }
    }
  }

  private fun colorButton(button: Button, color: Colors){
//    println("COLORING: $color...")
    when(color){
      Colors.WHITE -> button.background.setTint(Color.parseColor("#FFFFFF"))
      Colors.ORANGE -> button.background.setTint(Color.parseColor("#EEA222"))
      Colors.GREEN -> button.background.setTint(Color.parseColor("#77CC99"))
      Colors.RED -> button.background.setTint(Color.parseColor("#CC0000"))
      Colors.LIGHTRED -> button.background.setTint(Color.parseColor("#CC7777"))
      Colors.BLUE -> button.background.setTint(Color.parseColor("#0000CC"))
      Colors.RESETLIGHT -> button.background.setTint(Color.parseColor("#E2E2FF"))
      Colors.RESETMID -> button.background.setTint(Color.parseColor("#C2C2EE"))
      Colors.RESETDARK -> button.background.setTint(Color.parseColor("#9999CC"))
      Colors.MEMORYSETUP -> button.background.setTint(Color.parseColor("#2266DD"))
      Colors.GROUPSETUP -> button.background.setTint(Color.parseColor("#FFCC55"))
      Colors.LIGHTGROUPSETUP -> button.background.setTint(Color.parseColor("#FFDFB0"))
    }
  }

  private fun getAllButtons(v: ViewGroup) {
    for(i in 0..v.childCount){
      val child = v.getChildAt(i)
      if(child is Button){
        buttons.add(child)
      } else if (child is ViewGroup) getAllButtons(child)
    }
  }

  private fun switchCounterButton(counterButtons: ArrayList<ToggleButton>){
    if(binding.autoChangeButton.isChecked){
      isChanging = true
      for(i in counterButtons.indices){
        if(counterButtons[i].isChecked){
          counterButtons[i].isChecked = false
          colorButton(counterButtons[i], Colors.RESETDARK)
          if(i == (counterButtons.size-1)){
            counterButtons[0].isChecked = true
            colorButton(counterButtons[0], Colors.WHITE)
          } else {
            counterButtons[i+1].isChecked = true
            colorButton(counterButtons[i+1], Colors.WHITE)
          }
          isChanging = false
          return
        }
      }
    }
  }

  enum class Colors {
    WHITE, RED, BLUE, ORANGE, GREEN, LIGHTRED, RESETLIGHT, RESETMID, RESETDARK, MEMORYSETUP, GROUPSETUP, LIGHTGROUPSETUP
  }

  override fun onLongClick(view: View?): Boolean {
    println("ON LONG CLICK")
    if (binding.memoryButton.isChecked){
      println("ISSETTING: ${isSettingGroups[4]}")
      binding.memoryButton.isChecked = false
      colorButton(binding.memoryButton, Colors.MEMORYSETUP)
      onLongClick(view)
      return true
    }
    if (view is Button) {
      for (i in groupButtons.indices){
        if (view == groupButtons[i]) {
          isSettingGroups[i] = !isSettingGroups[i]
          if (isSettingGroups[i]) {
            setGroups(view)
            setCounters(i)
            colorButton(view, Colors.GROUPSETUP)
          } else {
            if (isSettingGroups[4]) {
              if (memoryArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
              else colorButton(groupButtons[i], Colors.RESETMID)
            } else {
              if (groupArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
              else colorButton(groupButtons[i], Colors.RESETMID)
            }
            resetCounters()
            for (groupButton in groupButtons) groupButton.isChecked = false
            for (counterButton in counterButtons) counterButton.isChecked = false
          }
          return true
        }
      }
      isSettingGroups[4] = !isSettingGroups[4]
      binding.memoryButton.isChecked = false
      if (isSettingGroups[4]) {
        for (i in groupButtons.indices){
          if (groupButtons[i].isChecked) {
            memoryIndicator = true
            groupButtons[i].isChecked = false
            memoryIndicator = false
          }
        }
        resetButtonsExcept()
        colorGroupsWithMembers(true)
        colorButton(view, Colors.MEMORYSETUP)
      } else {
        stopSetting()
        for (groupButton in groupButtons) groupButton.isChecked = false
        for (counterButton in counterButtons) counterButton.isChecked = false
        colorButton(view, Colors.RESETLIGHT)
      }
    }
    return true
  }

  private fun setCounters(groupIndex: Int) {
    if(isSettingGroups[4]){
    println("--- SET COUNTERS ---")
    println("CLICKED GROUP INDEX: $groupIndex")
      setCountersM@for (i in 0..7){
        println("ITERATION $i")
        println("Memory groups, counter button ${i+1}: [${memoryArray[0][i]}, ${memoryArray[1][i]}, ${memoryArray[2][i]}, ${memoryArray[3][i]}]\n")
        if (!(memoryArray[0][i] or memoryArray[1][i] or memoryArray[2][i] or memoryArray[3][i])){
          println("BUTTON ${i+1} IS NOT IN ANY GROUP")
          if(!counterButtons[i].isChecked) colorButton(counterButtons[i], Colors.GREEN)
          counterButtons[i].isChecked = false
          continue@setCountersM
        }

        if (memoryArray[groupIndex][i]){
          println("BUTTON IS IN GROUP $groupIndex, coloring white...")
          counterButtons[i].isChecked = true
        } else {
          for (j in 0..3) {
            println("CHECKING GROUP $j...")
            if (memoryArray[j][i]){
              println("BUTTON IS IN GROUP $j, coloring red...")
              if(!counterButtons[i].isChecked) colorButton(counterButtons[i], Colors.LIGHTRED)
              counterButtons[i].isChecked = false
              continue@setCountersM
            }
          }
        }
      }
    } else {
      setCountersG@for (i in 0..7){
        println("ITERATION $i")
        println("Memory groups, counter button ${i+1}: [${memoryArray[0][i]}, ${memoryArray[1][i]}, ${memoryArray[2][i]}, ${memoryArray[3][i]}]\n")
        if (!(groupArray[0][i] or groupArray[1][i] or groupArray[2][i] or groupArray[3][i])){
          println("BUTTON ${i+1} IS NOT IN ANY GROUP")
          if(!counterButtons[i].isChecked) colorButton(counterButtons[i], Colors.GREEN)
          counterButtons[i].isChecked = false
          continue@setCountersG
        }
        if (groupArray[groupIndex][i]){
          println("BUTTON IS IN GROUP $groupIndex, coloring white...")
          counterButtons[i].isChecked = true
        } else {
          for (j in 0..3) {
            println("CHECKING GROUP $j...")
            if (groupArray[j][i]){
              println("BUTTON IS IN GROUP $j, coloring red...")
              if(!counterButtons[i].isChecked) colorButton(counterButtons[i], Colors.LIGHTRED)
              counterButtons[i].isChecked = false
              continue@setCountersG
            }
          }
        }
      }
    }
  }

  private fun resetCounters(){
    for (i in counterButtons.indices){
      colorButton(counterButtons[i], Colors.RESETDARK)
    }
  }

  private fun checkGroup(index: Int){
    groupButtons[index].isChecked = true
    colorButton(groupButtons[index], Colors.ORANGE)
    if (binding.memoryButton.isChecked) {
      when (index) {
        0 -> {
          resetCounters()
          for (i in memoryArray[0].indices){
            if (memoryArray[0][i]){
              counterButtons[i].isChecked = true
              colorButton(counterButtons[i], Colors.WHITE)
            }
          }
        }
        1 -> {
          resetCounters()
          for (i in memoryArray[1].indices){
            if (memoryArray[1][i]){
              counterButtons[i].isChecked = true
              colorButton(counterButtons[i], Colors.WHITE)
            }
          }
        }
        2 -> {
          resetCounters()
          for (i in memoryArray[2].indices){
            if (memoryArray[2][i]){
              counterButtons[i].isChecked = true
              colorButton(counterButtons[i], Colors.WHITE)
            }
          }
        }
        3 -> {
          resetCounters()
          for (i in memoryArray[3].indices){
            if (memoryArray[3][i]){
              counterButtons[i].isChecked = true
              colorButton(counterButtons[i], Colors.WHITE)
            }
          }
        }
      }
    } else {
      when (index) {
        0 -> {
          resetCounters()
          for (i in groupArray[0].indices){
            if (groupArray[0][i]){
              counterButtons[i].isChecked = true
              colorButton(counterButtons[i], Colors.WHITE)
            }
          }
        }
        1 -> {
          resetCounters()
          for (i in groupArray[1].indices){
            if (groupArray[1][i]){
              counterButtons[i].isChecked = true
              colorButton(counterButtons[i], Colors.WHITE)
            }
          }
        }
        2 -> {
          resetCounters()
          for (i in groupArray[2].indices){
            if (groupArray[2][i]){
              counterButtons[i].isChecked = true
              colorButton(counterButtons[i], Colors.WHITE)
            }
          }
        }
        3 -> {
          resetCounters()
          for (i in groupArray[3].indices){
            if (groupArray[3][i]){
              counterButtons[i].isChecked = true
              colorButton(counterButtons[i], Colors.WHITE)
            }
          }
        }
      }
    }
  }

  private fun stopSetting(isMemory: Boolean = false){
    if (isMemory) {
      for (i in 0..3) isSettingGroups[i] = false
    }
    else {
      for (i in isSettingGroups.indices) isSettingGroups[i] = false
    }
    resetCounters()
    for (i in groupButtons.indices) colorButton(groupButtons[i], Colors.RESETMID)
  }

  private fun setGroups(view: Button) {
      when(view){
        groupButtons[0] -> {
          for (i in (1..3)){
            isSettingGroups[i] = false
            if (isSettingGroups[4]) {
              if (memoryArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
              else colorButton(groupButtons[i], Colors.RESETMID)
            } else {
              if (groupArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
              else colorButton(groupButtons[i], Colors.RESETMID)
            }
          }
        }
        groupButtons[1] -> {
          for (i in (0..3).filterNot { it == 1 }){
            isSettingGroups[i] = false
            if (isSettingGroups[4]) {
              if (memoryArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
              else colorButton(groupButtons[i], Colors.RESETMID)
            } else {
              if (groupArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
              else colorButton(groupButtons[i], Colors.RESETMID)
            }

          }
        }
        groupButtons[2] -> {
          for (i in ((0..3).filterNot { it == 2 })){
            isSettingGroups[i] = false
            if (isSettingGroups[4]) {
              if (memoryArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
              else colorButton(groupButtons[i], Colors.RESETMID)
            } else {
              if (groupArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
              else colorButton(groupButtons[i], Colors.RESETMID)
            }
          }
        }
        groupButtons[3] -> {
          for (i in 0..2){
            isSettingGroups[i] = false
            if (isSettingGroups[4]) {
              if (memoryArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
              else colorButton(groupButtons[i], Colors.RESETMID)
            } else {
              if (groupArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
              else colorButton(groupButtons[i], Colors.RESETMID)
            }
          }
        }
      }
  }

  private fun colorGroupsWithMembers(isMemory: Boolean){
    for (i in groupButtons.indices) {
      if (isMemory) {
        if (memoryArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
        else colorButton(groupButtons[i], Colors.RESETMID)
      } else {
        if (groupArray[i].any { it }) colorButton(groupButtons[i], Colors.LIGHTGROUPSETUP)
        else colorButton(groupButtons[i], Colors.RESETMID)
      }
    }
  }
}