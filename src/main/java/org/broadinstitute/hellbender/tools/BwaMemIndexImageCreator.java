package org.broadinstitute.hellbender.tools;

import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.broadinstitute.hellbender.cmdline.CommandLineProgram;
import org.broadinstitute.hellbender.cmdline.StandardArgumentDefinitions;
import org.broadinstitute.hellbender.cmdline.programgroups.BwaMemUtilitiesProgramGroup;
import org.broadinstitute.hellbender.exceptions.UserException;
import org.broadinstitute.hellbender.utils.bwa.BwaMemIndex;

import java.io.File;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Simply creates the reference index image file.
 */
@CommandLineProgramProperties(
        summary = "Creates the image file for use by BwaMemAligner",
        oneLineSummary = "Creates the image file for use by BwaMemAligner",
        programGroup = BwaMemUtilitiesProgramGroup.class
)
public final class BwaMemIndexImageCreator extends CommandLineProgram {

    public static final String USE_EXISTING_INDEX_FULL_NAME = "useExistingIndexFilesIfAvailable";
    public static final String USE_EXISTING_INDEX_SHORT_NAME = "useIndexFiles";

    @Argument(fullName = StandardArgumentDefinitions.INPUT_LONG_NAME,
            shortName = StandardArgumentDefinitions.INPUT_SHORT_NAME,
            doc = "Input reference fasta file. The five bwa index files are assumed living in the same directory with the same prefix.")
    private String referenceFastaLoc = null;

    @Argument(fullName = StandardArgumentDefinitions.OUTPUT_LONG_NAME,
            shortName = StandardArgumentDefinitions.OUTPUT_SHORT_NAME,
            doc = "Output reference index image file (ending in \".img\").",
            optional = true)
    private String referenceIndexImageOutputLoc = null;

    @Argument(fullName = USE_EXISTING_INDEX_FULL_NAME,
            shortName = USE_EXISTING_INDEX_SHORT_NAME,
            doc = "If there are some index files present, build the image out of those rather than the reference.",
            optional = true)
    private boolean useExistingIndexFilesIfAvailable = false;

    @Override
    protected final Object doWork() {
        if (referenceIndexImageOutputLoc == null) {
            referenceIndexImageOutputLoc = referenceFastaLoc + ".img";
        }

        if (useExistingIndexFilesIfAvailable) {
            if (checkIndexFilesArePresent(referenceFastaLoc)) {
                BwaMemIndex.createIndexImageFromIndexFiles(referenceFastaLoc, referenceIndexImageOutputLoc);
                return null;
            } else {
                logger.warn(String.format("User requested to use existing index files, " +
                        "however no complete set of index file (%s.*) was found; " +
                        "trying to create the image from the reference file directly", referenceFastaLoc));
            }
        } else {
            logger.warn("User requested to use existing index files however no complete set was found");
        }
        BwaMemIndex.createIndexImageFromFastaFile(referenceFastaLoc, referenceIndexImageOutputLoc);
        return null;
    }

    private boolean checkIndexFilesArePresent(final String prefix) {
        return !BwaMemIndex.INDEX_FILE_EXTENSIONS.stream()
                .map(ext -> new File(prefix + ext))
                .anyMatch(file -> !file.exists());
    }
}
