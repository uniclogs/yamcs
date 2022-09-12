package org.oresat.mdb;

import org.yamcs.xtce.Argument;
import org.yamcs.xtce.ArgumentAssignment;
import org.yamcs.xtce.ArgumentType;
import org.yamcs.xtce.CommandContainer;
import org.yamcs.xtce.MetaCommand;

public class EdlCommand extends MetaCommand {

    public EdlCommand(String name, ArgumentType argType, String code) {
        super(name);
        this.setCommand(argType, code);
        this.setCommandContainer(new CommandContainer("EDL_Command_Packet"));
        this.setQualifiedName("/dev/" + name);
    }

    private void setCommand(ArgumentType argType, String code) {
        Argument arg = new Argument("Command_Code");
        arg.setArgumentType(argType);
        this.addArgument(arg);
        this.addArgumentAssignment(new ArgumentAssignment("Command_Code", code));
    }
}
