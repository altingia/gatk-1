package org.broadinstitute.hellbender.tools;

import org.apache.spark.sql.catalyst.plans.logical.Except;
import org.broadinstitute.hellbender.BwaMemTestUtils;
import org.broadinstitute.hellbender.CommandLineProgramTest;
import org.broadinstitute.hellbender.utils.bwa.BwaMemIndex;
import org.broadinstitute.hellbender.utils.test.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test if image can be successfully created.
 */
public class BwaMemIndexImageCreatorIntegrationTest extends CommandLineProgramTest {

    private static final File TEST_DATA_DIR = new File("src/test/resources/large/");
    private static final File testReferenceFasta = new File(TEST_DATA_DIR, "human_g1k_v37.20.21.fasta");

    @Test
    public void testImageFileGeneration() throws Exception {

        final File tempImage = BaseTest.createTempFile("tempBwaMemIndexImage", ".img");
        final List<String> args = new ArrayList<>(Arrays.asList(
                "--input", testReferenceFasta.getAbsolutePath(),
                "--output", tempImage.getAbsolutePath()));
        runCommandLine(args);

        // piggy-backing on the existing integration test
        try( final BwaMemIndex index = new BwaMemIndex(tempImage.getAbsolutePath()) ){
            BwaMemTestUtils.assertCorrectSingleReadAlignment(index);
            BwaMemTestUtils.assertCorrectChimericContigAlignment(index);
        }
    }

    @Test
    public void testImageFileGenerationFromIndexFiles() throws Exception {
        final File imgFile = createTempFile("test-img-file", ".img");
        final List<String> args = new ArrayList<>(Arrays.asList(
                "--input", testReferenceFasta.getAbsolutePath(),
                "--output", imgFile.getAbsolutePath(),
                "--" + BwaMemIndexImageCreator.USE_EXISTING_INDEX_FULL_NAME));
        runCommandLine(args);
        Assert.assertTrue(imgFile.exists());
        try( final BwaMemIndex index = new BwaMemIndex(imgFile.getAbsolutePath()) ){
            BwaMemTestUtils.assertCorrectSingleReadAlignment(index);
            BwaMemTestUtils.assertCorrectChimericContigAlignment(index);
        }
        Assert.assertTrue(imgFile.delete());
    }

}
