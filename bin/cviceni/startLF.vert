#version 150
in vec2 inPosition;  
out vec3 normala;
out vec3 lightDirection;
out vec3 viewDirection;
out vec3 spot;
out float dist;
out vec4 totalAmbient;
out vec4 totalDiffuse;
out vec4 totalSpecular;
float zetko;
float PI = 3.14159;
uniform mat4 mat; 
uniform mat4 viewMat;
uniform vec3 eyePosition;
uniform vec3 lightPosition;
uniform int vertexMode;
uniform int functionType;

float getZetko(float x, float y){
	return cos(sqrt(x*x + y*y));
}

vec3 getCosFun(vec2 uv){
	vec3 positCos;
	uv -= 0.5; 		// zmena na <-0.5, 0.5>
	uv *= 2;		// zmena na <-1.0, 1.0>	
	uv *= 10;
	zetko = getZetko(uv.x, uv.y);
	positCos = vec3(uv.x, uv.y, zetko);
	return positCos;
}

vec3 sfericFunc1(vec2 uv){
	float t = uv.x * PI;
	float s = uv.y * 2 * PI;
	float R = 6;
	float azimuth = s;
	float zenit = t;
	
	uv.x = R * sin(zenit) * cos(azimuth);
	uv.y = R * sin(zenit) * sin(azimuth);
	zetko = R * cos(zenit);
	vec3 posit = vec3(uv.x,uv.y,zetko);
	return posit;			
}

vec3 function(vec2 uv){
	vec3 posit;
	switch(functionType){
		case 1:
			posit = getCosFun(uv);
			break;		
		case 2:
			posit = sfericFunc1(uv);
			break;				
	}		
	return posit;
}

vec3 normalDiff(vec2 uv){
	float delta = 0.01;
	vec3 dzdu= (function(uv+vec2(delta,0))-function(uv-vec2(delta,0)))/2.0/delta;
	vec3 dzdv= (function(uv+vec2(0,delta))-function(uv-vec2(0,delta)))/2.0/delta;
	return cross(dzdu,dzdv);
}

void main() {
	vec3 position = function(inPosition);
	vec3 normal = normalDiff(inPosition.xy);
	mat3 normalMatrix = transpose(inverse(mat3(viewMat)));
	normal = normalMatrix*normal;
	
	vec4 objectPosition = viewMat * vec4(position.xyz, 1.0);		
	vec4 lightPos = viewMat * vec4(lightPosition.xyz, 1.0);
	vec4 spotDirCam = viewMat * vec4(eyePosition, 1.0);
	
	spot = spotDirCam.xyz;
	spot = vec3(0,0,-1);	
			
	lightDirection = lightPos.xyz - objectPosition.xyz;
	viewDirection = -objectPosition.xyz;	
	dist = length(lightDirection); 
	
	
	
	switch(vertexMode){
		case 1:
			vec3 ld = normalize(lightDirection); 
			vec3 nd = normalize(normal);
			vec3 vd = normalize(viewDirection);	
			float NdotL = max(dot(ld,nd),0.0);
			
			vec3 halfVector = normalize( ld + vd);
			float NdotH = max(0.0 , dot( nd, halfVector));
			
							//material 
			vec4 mambient = vec4(1.0,0.7,0.7, 1.0); 
			vec4 mdiffuse = vec4(0.7,0.7,0.7, 1.0); 
			vec4 mspecular = vec4(1.0,1.0,1.0, 1.0); 
							//svìtlo 
			vec4 lambient = vec4(0.2,0.2,0.2, 1.0); 
			vec4 ldiffuse = vec4(0.8,0.8,0.8, 1.0); 
			vec4 lspecular = vec4(1.0,1.0,1.0, 1.0);
			float specularPower = 32.0;
			
			totalAmbient = mambient * lambient;
			totalDiffuse = NdotL * mdiffuse * ldiffuse; 
			totalSpecular =  mspecular * lspecular * (pow(NdotH, 4*specularPower));				
			break;
		case 2:
			normala = normal;
			break;
		case 3:
			normala = normal;
			break;
	}
	
	gl_Position = mat * vec4(position.x, position.y, position.z, 1.0);
		
} 

