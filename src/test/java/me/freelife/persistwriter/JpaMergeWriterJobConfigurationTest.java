package me.freelife.persistwriter;

import me.freelife.TestJobLauncher;
import me.freelife.reader.jdbc.Pay;
import me.freelife.reader.jdbc.PayRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static me.freelife.persistwriter.JpaMergeWriterJobConfiguration.JOB_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by jojoldu@gmail.com on 20/08/2018
 * Blog : http://jojoldu.tistory.com
 * Github : https://github.com/jojoldu
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class JpaMergeWriterJobConfigurationTest {

    @Autowired
    @Qualifier(JOB_NAME)
    private Job job;

    @Autowired
    private TestJobLauncher testJobLauncher;

    @Autowired
    private PayRepository payRepository;

    @Autowired
    private PayCopyRepository payCopyRepository;

    @Test
    public void mergeTest() throws Exception {
        //given
        for(long i=0;i<10;i++) {
            payRepository.save(new Pay(i*100, String.valueOf(i), LocalDateTime.now()));
        }
        JobLauncherTestUtils jobLauncherTestUtils = testJobLauncher.getJobLauncherTestUtils(job);
        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString("version", "1");

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(builder.toJobParameters());

        //then
        assertThat(jobExecution.getStatus(), is(BatchStatus.COMPLETED));
        assertThat(payCopyRepository.findAll().size(), is(10));
    }

}
