/*
 *  Copyright (C) 2010-2022 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class ConvertAVM2Item extends AVM2Item {

    public GraphTargetItem type;

    public ConvertAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem value, GraphTargetItem type) {
        super(instruction, lineStartIns, value.getPrecedence(), value);
        this.type = type;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(type);
        if (value != null) {
            visitor.visit(value);
        }
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        boolean displayConvert = true;
        GraphTargetItem valueReturnType = value.returnType();
        /*if (valueReturnType instanceof TypeItem) {
            TypeItem ti = (TypeItem)valueReturnType;
            ti.fullTypeName.toRawString()
        }*/
        switch (type.toString()) {
            case "Boolean":
                displayConvert = !valueReturnType.equals(TypeItem.BOOLEAN) &&
                                 !valueReturnType.equals(TypeItem.UNBOUNDED);
                break;
            case "Number":
            case "int":
            case "uint":
                displayConvert = !valueReturnType.equals(TypeItem.INT) && 
                                !valueReturnType.equals(TypeItem.NUMBER) && 
                                !valueReturnType.equals(TypeItem.UINT) &&
                                !valueReturnType.equals(TypeItem.UNBOUNDED);
                break;
            case "String":
                displayConvert = !valueReturnType.equals(TypeItem.STRING) &&
                                 !valueReturnType.equals(TypeItem.UNBOUNDED);
                break;            
        }
        if (displayConvert) {
            type.toString(writer, localData).append("(");
        }
        value.toString(writer, localData);
        if (displayConvert) {
            writer.append(")");
        }
        return writer;
    }

    @Override
    public GraphTargetItem getNotCoerced() {
        return value.getNotCoerced();
    }

    @Override
    public GraphTargetItem getNotCoercedNoDup() {
        return value.getNotCoercedNoDup();
    }

    @Override
    public Object getResult() {
        switch (type.toString()) {
            case "Boolean":
                return EcmaScript.toBoolean(value.getResult());
            case "Number":
                return value.getResultAsNumber();
            case "int":
                return EcmaScript.toInt32(value.getResultAsNumber());
            case "uint":
                return EcmaScript.toUint32(value.getResult());
            case "String":
                return value.getResultAsString();
            case "Object":
                return value.getResult(); //if not object throw TypeError
            default:
                return new Object();
        }
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (dependencies.contains(value)) {
            return false;
        }
        dependencies.add(value);
        return value.isConvertedCompileTime(dependencies);
    }

    @Override
    public GraphTargetItem returnType() {
        return type;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConvertAVM2Item other = (ConvertAVM2Item) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

}
