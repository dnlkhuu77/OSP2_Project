Name: Daniel Khuu
ID: 109372156

When running, there is a couple of "Completed successfully but will issue warnings". Run it a couple of times until it run successfully without any warnings.

Statistics:

There were multiple readings from the multitude of threads, so I will choose the results of the lowest running thread to explain the difference in statistics between my solution and the Demo.

There were 3 devices: Disk 1 (/etc/go/), Disk 2 (/etc/), and the Swap Device. The Disks results will be in the format: (Disk 1, Disk 2).

According to the results and calculation, there is around 2 tracks per cylinder.

For the Demo, the total number of tracks swept is around (230, 58) for the Disks and 23200 for the Swap Device. Average number of tracks swept per I/O request is (4, 2) for the Disks and 16 for the Swap Device. The average turnaround time for I/O requests is (221, 845) for the Disks and 1056 for the Swap Device. The system throughput is around 0.0022184295.

For my solution, the total number of cylinders (these patterns should apply to tracks as well) swept is around (250, 64) for the Disks and 24194 for the Swap Device. Average number of tracks swept per I/O request is (4, 2) for the Disks and 16 for the Swap Device. The average turnaround time is (374, 869) for the Disks and 1285 for the Swap Device. The system throughput is 0.00153425163.

According to the results of these results, the performance of CSCAN and FIFO are not that much different but FIFO is just a bit faster because with C-SCAN you must move the head all the way to the front of the queue when the head is at the end. And if the head must move to the front of the queue a lot of times, C-SCAN can be slower than FIFO.