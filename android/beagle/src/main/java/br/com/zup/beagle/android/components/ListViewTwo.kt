/*
 * Copyright 2020 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.beagle.android.components

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.zup.beagle.android.action.Action
import br.com.zup.beagle.android.action.OnInitableComponent
import br.com.zup.beagle.android.context.Bind
import br.com.zup.beagle.android.context.ContextComponent
import br.com.zup.beagle.android.context.ContextData
import br.com.zup.beagle.android.context.normalizeContextValue
import br.com.zup.beagle.android.utils.generateViewModelInstance
import br.com.zup.beagle.android.utils.observeBindChanges
import br.com.zup.beagle.android.utils.toAndroidId
import br.com.zup.beagle.android.view.ViewFactory
import br.com.zup.beagle.android.view.viewmodel.ScreenContextViewModel
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.android.widget.WidgetView
import br.com.zup.beagle.annotation.RegisterWidget
import br.com.zup.beagle.core.IdentifierComponent
import br.com.zup.beagle.core.MultiChildComponent
import br.com.zup.beagle.core.ServerDrivenComponent
import br.com.zup.beagle.core.SingleChildComponent
import br.com.zup.beagle.core.Style
import br.com.zup.beagle.widget.core.Flex
import br.com.zup.beagle.widget.core.FlexDirection
import br.com.zup.beagle.widget.core.ListDirection
import org.json.JSONObject

@RegisterWidget
internal data class ListViewTwo(
    val direction: ListDirection,
    override val context: ContextData? = null,
    override val onInit: List<Action>? = null,
    val dataSource: Bind<List<Any>>,
    val template: ServerDrivenComponent,
    val onScrollEnd: List<Action>? = null,
    val scrollThreshold: Int? = null,
    val useParentScroll: Boolean = false,
    val iteratorName: String? = null,
    val key: String? = null
) : WidgetView(), ContextComponent, OnInitableComponent {

    @Transient
    private val viewFactory: ViewFactory = ViewFactory()

    @Transient
    private lateinit var contextAdapter: ListViewContextAdapter2

    @Transient
    private var list: List<Any>? = null

    @Transient
    private var onInitCalled = false

    @Transient
    private var canScrollEnd = true

    override fun buildView(rootView: RootView): View {
        val recyclerView = viewFactory.makeRecyclerView(rootView.getContext())
        recyclerView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View?) {
            }

            override fun onViewAttachedToWindow(v: View?) {
                if (!onInitCalled) {
                    onInit?.forEach { action ->
                        action.execute(rootView, recyclerView)
                    }
                    onInitCalled = true
                }
            }
        })
        val orientation = toRecyclerViewOrientation()
        contextAdapter = ListViewContextAdapter2(template, iteratorName, key, viewFactory, orientation, rootView)
        recyclerView.apply {
            setHasFixedSize(true)
            adapter = contextAdapter
            layoutManager = LinearLayoutManager(context, orientation, false)
            isNestedScrollingEnabled = useParentScroll
        }
        configDataSourceObserver(rootView, recyclerView)
        configRecyclerViewScrollListener(recyclerView, rootView)

        return recyclerView
    }

    private fun toRecyclerViewOrientation() = if (direction == ListDirection.VERTICAL) {
        RecyclerView.VERTICAL
    } else {
        RecyclerView.HORIZONTAL
    }

    private fun configDataSourceObserver(rootView: RootView, recyclerView: RecyclerView) {
        observeBindChanges(rootView, recyclerView, dataSource) { value ->
            if (value != list) {
                if (value.isNullOrEmpty()) {
                    contextAdapter.clearList()
                } else {
                    contextAdapter.setList(value)
                }
                list = value
                canScrollEnd = true
            }
        }
    }

    private fun configRecyclerViewScrollListener(
        recyclerView: RecyclerView,
        rootView: RootView
    ) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                onScrollEnd?.let {
                    if (canCallOnScrollEnd(recyclerView)) {
                        it.forEach { action ->
                            action.execute(rootView, recyclerView)
                        }
                        canScrollEnd = false
                    }
                }
            }
        })
    }

    private fun canCallOnScrollEnd(recyclerView: RecyclerView): Boolean {
        val reachEnd = scrollThreshold?.let {
            val scrolledPercent = calculateScrolledPercent(recyclerView)
            scrolledPercent >= scrollThreshold
        } ?: !run {
            if (direction == ListDirection.VERTICAL) {
                recyclerView.canScrollVertically(1)
            } else {
                recyclerView.canScrollHorizontally(1)
            }
        }
        return reachEnd && canScrollEnd
    }

    private fun calculateScrolledPercent(recyclerView: RecyclerView): Float {
        var scrolledPercentage: Float
        (recyclerView.layoutManager as LinearLayoutManager).apply {
            val totalItemCount = itemCount
            val lastVisible = findLastVisibleItemPosition().toFloat()
            scrolledPercentage = (lastVisible / totalItemCount) * 100
        }
        return scrolledPercentage
    }
}

internal class ListViewContextAdapter2(
    private val template: ServerDrivenComponent,
    private val iteratorName: String? = null,
    private val key: String? = null,
    private val viewFactory: ViewFactory,
    private val orientation: Int,
    private val rootView: RootView,
    private var listItems: ArrayList<Any> = ArrayList()
) : RecyclerView.Adapter<ContextViewHolderTwo>() {

    private val viewModel = rootView.generateViewModelInstance<ScreenContextViewModel>()

    private val listCellData = mutableMapOf<Int, CellData>()

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ContextViewHolderTwo {
//        val template = BeagleSerializer().serializeComponent(template)
//        val newTemplate = BeagleSerializer().deserializeComponent(template)

        val view = viewFactory.makeBeagleFlexView(
            rootView,
            Style(flex = Flex(flexDirection = flexDirection()))
        ).apply {
            layoutParams = RecyclerView.LayoutParams(layoutParamWidth(), layoutParamHeight())
            addServerDrivenComponent(template)
        }
        return ContextViewHolderTwo(view, template)
    }

    private fun layoutParamWidth() = if (isOrientationVertical()) MATCH_PARENT else WRAP_CONTENT

    private fun layoutParamHeight() = if (isOrientationVertical()) WRAP_CONTENT else MATCH_PARENT

    private fun flexDirection() = if (isOrientationVertical()) FlexDirection.COLUMN else FlexDirection.ROW

    private fun isOrientationVertical() = (orientation == RecyclerView.VERTICAL)

    private fun getContextDataId() = iteratorName ?: "item"

    override fun onBindViewHolder(holder: ContextViewHolderTwo, position: Int) {
        listCellData[position]?.let { cellData ->
            if (listItems[position] != cellData.contextData.value) {
                val contextData = ContextData(id = getContextDataId(), value = listItems[position])
                onBind(holder, cellData.copy(contextData = contextData))
            } else {
                onBind(holder, cellData)
            }
        } ?: run {
            val cellData = CellData(
                id = View.generateViewId(),
                contextData = ContextData(id = getContextDataId(), value = listItems[position])
            )
            updateIdToEachSubView(holder.viewsWithId, position)
            listCellData[position] = cellData
            onBind(holder, cellData)
        }
    }

    private fun onBind(holder: ContextViewHolderTwo, cellData: CellData) {
        holder.itemView.id = cellData.id
        viewModel.addContext(
            view = holder.itemView,
            contextData = cellData.contextData,
            shouldOverrideExistingContext = true
        )
    }

    private fun updateIdToEachSubView(viewsWithId: Map<String, View>, position: Int) {
        viewsWithId.forEach { (id, view) ->
            view.id = "$id:${getValueFromKey(position)}".toAndroidId()
        }
    }

    private fun getValueFromKey(position: Int) = key?.let {
        ((listItems[position]).normalizeContextValue() as JSONObject).get(it)
    } ?: position

    fun setList(list: List<Any>) {
        listItems = ArrayList(list)
        notifyDataSetChanged()
    }

    fun clearList() {
        val initialSize = listItems.size
        listItems.clear()
        notifyItemRangeRemoved(0, initialSize)
    }

    override fun getItemCount(): Int = listItems.size
}

internal class ContextViewHolderTwo(
    itemView: View,
    template: ServerDrivenComponent
) : RecyclerView.ViewHolder(itemView) {

    val viewsWithId = mutableMapOf<String, View>()

    init {
        getViewsWithId(template)
    }

    private fun getViewsWithId(template: ServerDrivenComponent) {
        when (template) {
            is SingleChildComponent -> getViewsWithId(template.child)
            is MultiChildComponent -> template.children.forEach { child ->
                getViewsWithId(child)
            }
            is IdentifierComponent -> {
                template.id?.let { id ->
                    viewsWithId.put(id, itemView.findViewById<View>(id.toAndroidId()))
                }
            }
        }
    }
}

internal data class CellData(
    val id: Int,
    val onInitCalled: Boolean = false,
    val contextData: ContextData
)