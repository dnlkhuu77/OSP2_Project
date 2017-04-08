Name: Daniel Khuu
ID: 109372156

Statistics:

There were multiple readings from the multitude of threads, so I will choose the results of the lowest running thread from both programs to explain the difference in statistics between my solution and the Demo.

For the Demo, the CPU Utilization is around 57.5% with the average service time per thread of 37357.76. The average normalized service time per thread is 0.050081514. And the program swapped in about 1200 pages and swapped out 300 pages.

For my solution, the CPU Utilization is around 81.5% with the average service time per thread of 32236.34. The average normalized service time per thread is 0.0623. And the program swapped in about 1000 pages and swapped out 484 pages.

From the results, the solution was able to use more of the CPU to shorten the time it takes memory and threads to do tasks. The average normalized service time was higher for my solution, which is ideal in performance. Overall, the LRU algorithm greatly improved the performance of memory over that used by the Demo.