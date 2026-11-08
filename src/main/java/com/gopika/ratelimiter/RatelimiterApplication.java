package com.gopika.ratelimiter;

import com.gopika.ratelimiter.model.entity.ClientConfig;
import com.gopika.ratelimiter.redis.RedisScriptExecutor;
import com.gopika.ratelimiter.service.RateLimiterFactory;
import com.gopika.ratelimiter.service.RateLimiterStrategy;
import com.gopika.ratelimiter.service.imp1.FixedWindowRateLimiter;
import com.gopika.ratelimiter.service.imp1.SlidingWindowLogRateLimiter;
import com.gopika.ratelimiter.service.imp1.TokenBucketRateLimiter;
import org.apache.catalina.util.RateLimiter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class RatelimiterApplication {

	public static void main(String[] args) {
		SpringApplication.run(RatelimiterApplication.class, args);
	}

//	@Bean
//	public CommandLineRunner testRedisScriptExecutor(RedisScriptExecutor redisScriptExecutor) {
//		return args -> {
//			Long result = redisScriptExecutor.executeScript(
//					"scripts/fixed_window.lua",
//					"ratelimit:test_java",
//					List.of("3", "60")
//			);
//			System.out.println("=== Redis Script Test Result: " + result + " ===");
//		};
//	}

//    @Bean
//    public CommandLineRunner commandLineRunner(FixedWindowRateLimiter rateLimiter) {
//        return args -> {
//            ClientConfig config = new ClientConfig("test_client", 3, 60, "FIXED_WINDOW");
//            boolean result = rateLimiter.isAllowed("test_client", config);
//            System.out.println("=== Fixed Window Test Result: " + result + " ===");
//        };
//    }

    // Since SpringBoot takes 15 - 30 seconds to run an application it shows wrong value
//    @Bean
//    public CommandLineRunner commandLineRunner(TokenBucketRateLimiter rateLimiter) {
//
//        return args -> {
//            ClientConfig config = new ClientConfig("test_client", 3, 60, "TOKEN_BUCKET");
//            boolean result = rateLimiter.isAllowed("test_client", config);
//            System.out.println("=== Token Bucket Test Result: " + result + " ===");
//        };
//    }

//    @Bean
//    public CommandLineRunner commandLineRunner(TokenBucketRateLimiter rateLimiter) {
//        return args -> {
//            ClientConfig config = new ClientConfig("test_client", 3, 60, "TOKEN_BUCKET");
//            for(int i=1; i<=5; i++) {
//                boolean result = rateLimiter.isAllowed("test_client", config);
//                System.out.println("=== Token Bucket Test Result ===" );
//                System.out.println("=== Attempt " + i + ": " + result + " ===");
//            }
//        };
//    }

//    @Bean
//    public CommandLineRunner commandLineRunner(SlidingWindowLogRateLimiter rateLimiter) {
//        return args -> {
//            ClientConfig config = new ClientConfig("test_client", 3, 60, "SLIDING_WINDOW_LOG");
//            for(int i=1; i<=5; i++) {
//                boolean result = rateLimiter.isAllowed("test_client", config);
//                System.out.println("=== Sliding Window Test Result ===");
//                System.out.println("=== Attempt " + i + ": " + result + " ===");
//
//            }
//        };
//    }

//	@Bean
//	public CommandLineRunner commandLineRunner(RateLimiterFactory factory) {
//		return args -> {
//			ClientConfig config = new ClientConfig("test_client", 3, 60, "FIXED_WINDOW");
//
//			RateLimiterStrategy strategy = factory.getStrategy("FIXED_WINDOW");
//			boolean result = strategy.isAllowed("test_client", config);
//			System.out.println("=== Factory returned: " + strategy.getClass().getSimpleName() + ", Result: " + result + " ===");
//		};
//	}

//	@Bean
//	public CommandLineRunner commandLineRunner(RateLimiterFactory factory) {
//		return args -> {
//			String[] algorithms = {"FIXED_WINDOW", "TOKEN_BUCKET", "SLIDING_WINDOW_LOG"};
//
//			for (String algorithm : algorithms) {
//				ClientConfig config = new ClientConfig("test_client", 3, 60, algorithm);
//				RateLimiterStrategy strategy = factory.getStrategy(algorithm);
//				boolean result = strategy.isAllowed("test_client", config);
//				System.out.println("=== Requested: " + algorithm + " | Factory returned: " + strategy.getClass().getSimpleName() + " | Result: " + result + " ===");
//			}
//		};
//	}

}
