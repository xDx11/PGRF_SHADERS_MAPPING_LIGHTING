# PGRF_SHADERS_MAPPING_LIGHTING
Computer Graphic - Java OpenGL - Student project (PGRF3 - FIM - UHK) - Shaders - Mapping and Lighting

Date: 2016

More information in [Wiki...](https://github.com/xDx11/PGRF_SHADERS_MAPPING_LIGHTING/wiki)

Shader - Per Pixel

	- Model
		- GRID
		- Cartesian function 3x
		- Cylinder function 3x
		- Sferic function 3x
	- Light mode
		- Position
		- Color
		- Normal
		- Normal
		- Blinn-Phong - Half vector
		- Blinn-Phong - Reflect vector
		- Normal mapping
		- Parallax mapping
	- Polygon mode
		- Fill
		- Line
	- Reflector
		- ON
			- Reflector Light source from user view
		- OFF
			- fixed point light on position (20, 20, 10)


Shader - Per Vertex & Per Pixel LIGHTING

	- Model
		- Cartesian function 1x
		- Sferic function 1x
	- Polygon mode
		- Fill
		- Line
	- Light
		- Vertex - Blinn-Phong - Half Vector
		- Fragment - Blinn-Phong - Reflect vector
		- Fragment - Blinn-Phong - Half Vector
	- Reflector
		- ON
			- Reflector Light source from user view
		- OFF
			- fixed point light on position (20, 20, 10)
