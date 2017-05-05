#version 150
in vec2 inPosition; 
out vec3 normal;
out vec3 transNormal;
out vec3 lightDirection;
out vec3 ldForReflect;
out vec3 viewDirection;
out vec3 spotDirection;
out vec3 position;
out vec2 textCoords;
out mat3 normalMatrix;
out vec3 lightVector;
out float dist;
float zetko;
float PI = 3.141592653589793238462643383279;
uniform mat4 mat; 
uniform mat4 viewMat;
uniform mat4 projMat;
uniform vec3 lightPosition;
uniform vec3 spotDir;
uniform vec3 eyePosition;
uniform int lightMode;
uniform int functionType;

float getZetko(float x, float y){
	return cos(sqrt(x*x + y*y));
}

vec3 getNavySailCartesianFunc2(vec2 uv){
	uv -= 0.5; 		// zmena na <-0.5, 0.5>
	uv *= 2;		// zmena na <-1.0, 1.0>
	
	float s = uv.x;
	float t = uv.y;
	vec3 positSail;
	positSail.x = cos(s)*cos(s)+cos(t)*cos(s)*8;
	positSail.y = sin(s)+cos(t)*sin(s)*2;
	positSail.z = sin(t)*cos(s)*3;
	positSail = positSail*2;
	return positSail;
}

vec3 getOwnCartesianFunc3(vec2 uv){
	float s = uv.x * 2 * PI;
	float t = uv.y * 2 * PI;
	vec3 pos;
	pos.x = cos(s) + cos(t) * cos(s);
	pos.y = sin(s) + cos(t) * sin(s);
	pos.z = sin(t);
	pos *= 2;	
	return pos;
}

vec3 getCosCartesianFunc1(vec2 uv){
	vec3 positCos;
	uv -= 0.5; 		// zmena na <-0.5, 0.5>
	uv *= 2;		// zmena na <-1.0, 1.0>	
	uv *= 10;
	zetko = getZetko(uv.x, uv.y);
	positCos = vec3(uv.x, uv.y, zetko);
	return positCos;
}

vec3 cylinderFunc1(vec2 uv){
	float t = uv.x * 2 * PI;
	float s = uv.y * 2 * PI;
	
	float R = t;
	float azimuth = s;
	float v = 2 * sin(t);
	
	uv.x = R * cos(azimuth);
	uv.y = R * sin(azimuth);
	zetko = v;
	
	vec3 posit = vec3(uv.x, uv.y, zetko);
	return posit;
}

vec3 cylinderFunc2(vec2 uv){
	uv.x *= 2 * PI;
	uv.y *= 2 * PI;
	float t = uv.x; 
	float s = uv.y;			
	
	float R = (1+max(sin(t),0))*0.5*t;
	float azimuth = s;
	float v = 3-t;
	
	uv.x = R * cos(azimuth);
	uv.y = R * sin(azimuth);
	zetko = v;

	vec3 posit = vec3(uv.x, uv.y, zetko);
	return posit;
}

vec3 cylinderFunc3(vec2 uv){		
	uv.x *= 2*PI;
	uv.x -= PI;
	uv.y *= 2*PI;
	float t = uv.x; 
	float s = uv.y;	
		
	float R = sin(t)-cos(t);
	float azimuth = s+sin(t)-cos(t);
	float v = t+sin(t);
	
	uv.x = -R * cos(azimuth);
	uv.y = R * sin(azimuth);
	zetko = v;

	vec3 posit = vec3(uv.x, uv.y, zetko);
	return posit;
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

vec3 sfericFunc2(vec2 uv){
	float t = uv.x * PI;
	float s = uv.y * 2 * PI;
	float R = 3 + cos(4*s);
	float azimuth = s;
	float zenit = t;
	
	uv.x = R * sin(zenit) * cos(azimuth);
	uv.y = R * sin(zenit) * sin(azimuth);
	zetko = R * cos(zenit);
	vec3 posit = vec3(uv.x,uv.y,zetko);
	return posit;
}

vec3 sfericFunc3(vec2 uv){
	float t = uv.x * 4;
	t -= 1;
	float s = uv.y * 2 * PI;
	float R = t;
	float zenit = -0.5*s*cos(t);
	float azimuth = 2*s*t*0.3*sin(t);
	
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
			posit = 10*vec3(uv.x, uv.y, 0.0);
			break;
		case 2:
			posit = getCosCartesianFunc1(uv);
			break;
		case 3:
			posit = getNavySailCartesianFunc2(uv);
			break;
		case 4:
			posit = getOwnCartesianFunc3(uv);
			break;		
		case 5:
			posit = cylinderFunc1(uv);
			break;
		case 6:
			posit = cylinderFunc2(uv);
			break;
		case 7:
			posit = cylinderFunc3(uv);
			break;
		case 8:
			posit = sfericFunc1(uv);
			break;		
		case 9:
			posit = sfericFunc2(uv);
			break;
		case 10:
			posit = sfericFunc3(uv);
			break;
	}		
	return posit;
}

vec3 normalDiff(vec2 uv){
	float delta = 0.01;
	vec3 dzdu= (function(uv+vec2(delta,0))-function(uv-vec2(delta,0)))/2.0/delta;   // (2.0*delta)
	vec3 dzdv= (function(uv+vec2(0,delta))-function(uv-vec2(0,delta)))/2.0/delta;
	return cross(dzdu,dzdv);
}

void main() {
	position = function(inPosition);
		
	if(functionType>4 && functionType < 10){
		textCoords = vec2(inPosition.y, inPosition.x);					
	} else {
		textCoords = inPosition;
		
	}
	normal = normalDiff(inPosition.xy);
	mat3 normalMatrix = transpose(inverse(mat3(viewMat)));	
	transNormal = normalize(normalMatrix * normal);		
	
	vec4 objectPosition = viewMat * vec4(position.xyz, 1.0);
	vec4 lightPosit = viewMat * vec4(lightPosition.xyz, 1.0);	
	lightDirection = lightPosit.xyz - objectPosition.xyz;
	viewDirection =  -objectPosition.xyz;
	ldForReflect = lightDirection;	// LightDirection vektor pro reflektor
	//vec4 spotDirCam = viewMat * vec4(spotDir, 1.0);
	//spotDirection = spotDirCam.xyz;					
	spotDirection = vec3(0,0,-1);	//spotDirection reflektoru z pohledu pozorovatele
	dist = length(lightDirection); 	// vzdalenost pro utlum svetla	
			 		
	switch(lightMode){	
		case 7:						
			float delta = 0.01;	 
			vec3 tangent = (function(inPosition+vec2(delta,0))-function(inPosition-vec2(delta,0)))/2.0/delta;
			tangent = normalize(normalMatrix*tangent);	
			
			vec3 bitangent = normalize(normalMatrix*(cross(transNormal, tangent)));			
			//mat3 TBN = mat3(normalize(tangent), normalize(bitangent), normalize(transNormal));
			
			vec3 viewVec;
			viewVec.x = dot(tangent, viewDirection);
			viewVec.y = dot(bitangent, viewDirection);
			viewVec.z = dot(transNormal, viewDirection);
			viewDirection = viewVec;
			
			vec3 lightVec;
			lightVec.x = dot(tangent, lightDirection);
			lightVec.y = dot(bitangent, lightDirection);
			lightVec.z = dot(transNormal, lightDirection);
			lightDirection = lightVec;

			break;
		case 8:
			float delta2 = 0.01;	 
			vec3 tangent2 = (function(inPosition+vec2(delta2,0))-function(inPosition-vec2(delta2,0)))/2.0/delta2;
			tangent2 = normalize(normalMatrix*tangent2);	
			
			vec3 bitangent2 = normalize(normalMatrix*(cross(transNormal, tangent2)));			
			//mat3 TBN = mat3(normalize(tangent), normalize(bitangent), normalize(transNormal));
			
			vec3 viewVec2;
			viewVec2.x = dot(tangent2, viewDirection);
			viewVec2.y = dot(bitangent2, viewDirection);
			viewVec2.z = dot(transNormal, viewDirection);
			viewDirection = viewVec2;
			
			vec3 lightVec2;
			lightVec2.x = dot(tangent2, lightDirection);
			lightVec2.y = dot(bitangent2, lightDirection);
			lightVec2.z = dot(transNormal, lightDirection);
			lightDirection = lightVec2;
						
	}
		
	gl_Position = projMat * viewMat * vec4(position.x, position.y, position.z, 1.0);
} 

