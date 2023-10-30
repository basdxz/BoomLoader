package com.basdxz.boomload.asm;

import com.basdxz.boomload.Tags;
import com.falsepattern.lib.asm.IClassNodeTransformer;
import com.falsepattern.lib.asm.SmartTransformer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@Accessors(fluent = true, chain = false)
public final class BoomTransformer implements SmartTransformer {
    static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + " ASM");

    @Getter
    private final List<IClassNodeTransformer> transformers = Collections.singletonList(new OpenGLFunctionDumper());

    @Getter
    private final Logger logger = LOG;
}
