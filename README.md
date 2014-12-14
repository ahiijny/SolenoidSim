Models the B field of multiple solenoids.

To run the program, run SollySim.jar. 

If prompted to choose a program to open the file, try Java/jre7/bin/javaw.exe. Or with jre8 or whatever.

The init.cfg file can be modified with text-editing software to customize the initial conditions.

*** Clarification: "Scale plot step" multiplies the number of points rendered by the specified scalar, so if the UI is too laggy, decrease the value in that field.

Works Consulted:

- "Magnetic Constant" : http://physics.nist.gov/cgi-bin/cuu/Value?mu0
  - For the calculations, using:
    - μ0 = 4 Math.PI E-7
    - note that in Java a 64-bit double gives ~16 significant figures of accuracy
- "Runge-Kutta Methods" : http://en.wikipedia.org/wiki/Runge%E2%80%93Kutta_methods
- "Runge-Kutta Algorithm" : http://www.myphysicslab.com/runge_kutta.html
- "List of Runge-Kutta Methods" : http://en.wikipedia.org/wiki/List_of_Runge%E2%80%93Kutta_methods
- "Biotsavart primer" : http://www.hmvb.org/biotsavart.pdf
- "Rotation matrix" : http://en.wikipedia.org/wiki/Rotation_matrix
- "Calculate rotation matrix to align vector a to vector b" : http://math.stackexchange.com/questions/180418/calculate-rotation-matrix-to-align-vector-a-to-vector-b-in-3d/476311
- "Java key bindings" : http://tips4java.wordpress.com/2013/06/09/motion-using-the-keyboard/

Also I sorta reused a lot of code from my gravity simulator at https://github.com/ahiijny/Rendezvous.

