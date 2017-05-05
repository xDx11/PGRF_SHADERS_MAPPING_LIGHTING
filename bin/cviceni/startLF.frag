#version 150
in vec3 normala;
in vec3 lightDirection;
in vec3 viewDirection;
in vec4 totalAmbient;
in vec4 totalDiffuse;
in vec4 totalSpecular;
in float dist;
in vec3 spot;
out vec4 outColor; 
vec3 nd, ld, vd;
float NdotL;
vec4 totalAmbient2;
vec4 totalDiffuse2;
vec4 totalSpecular2;
uniform vec3 eyePosition; // Vec3D ViewVector
uniform int reflectorMode;
uniform int vertexMode;

void main() {
	
	ld = normalize(lightDirection);
		
					//material 
	vec4 mambient = vec4(1.0,0.7,0.7, 1.0); 
	vec4 mdiffuse = vec4(0.7,0.7,0.7, 1.0); 
	vec4 mspecular = vec4(1.0,1.0,1.0, 1.0); 
					//svìtlo 
	vec4 lambient = vec4(0.2,0.2,0.2, 1.0); 
	vec4 ldiffuse = vec4(0.8,0.8,0.8, 1.0); 
	vec4 lspecular = vec4(1.0,1.0,1.0, 1.0);	
		
	float constantAttenuation = 1.0;
	float linearAttenuation = 0.001;
	float quadraticAttenuation = 0.001;
	float att = 1.0 / (constantAttenuation + linearAttenuation*dist + quadraticAttenuation*dist*dist);	
	//float specularPower = 32.0;
	float specularPower = 12.0;
	
	vec3 spotDirection = spot;		
	//vec3 spotDirection = -ld;	
	float spotCutOff = 0.95; // 0.99
	float cutOff = 0.91;
	
	
	switch(vertexMode){
		case 1:					
			if(reflectorMode == 1){
				float spotEffect = dot(normalize(spotDirection), (-ld));
				float epsilon = spotCutOff - 0.91;
				float intensity = clamp((spotEffect - spotCutOff) / epsilon, 0.0, 1.0);		
				if (spotEffect > spotCutOff){		
					outColor = totalAmbient + intensity*att*(totalDiffuse + totalSpecular);
				} else {
					outColor = totalAmbient;
				}
			} else {
				outColor = totalAmbient + att*(totalDiffuse + totalSpecular);
			}
			break;
		case 2:		
			nd = normalize(normala);
			vd = normalize(viewDirection);	
			NdotL = max(dot(ld, nd),0.0);
						
			vec3 reflection=normalize((( 2.0 * dot(ld, nd) * nd)) - ld);						
			float RdotV = max(0.0 , dot( vd, reflection));			
			
			totalAmbient2 = mambient * lambient;
			totalDiffuse2 = NdotL * mdiffuse * ldiffuse; 
			totalSpecular2 =  mspecular * lspecular * (pow(RdotV, specularPower));
			
			if(reflectorMode == 1){
				float spotEffect = dot(normalize(spotDirection), (-ld));				
				float epsilon = spotCutOff - cutOff;
				float intensity = clamp((spotEffect - spotCutOff) / epsilon, 0.0, 1.0);					
				if (spotEffect > spotCutOff){	
					outColor = totalAmbient2 + intensity * att * (totalDiffuse2 + totalSpecular2);
				} else {
					outColor = totalAmbient2;
				}
			} else {
				outColor = totalAmbient2 + att * (totalDiffuse2 + totalSpecular2);
			}
			break;
		case 3:
			nd = normalize(normala);
			vd = normalize(viewDirection);	
			NdotL = max(dot(ld,nd),0.0);
			
			vec3 halfVector = normalize( ld + vd);
			float NdotH = max(0.0 , dot( nd, halfVector));						 			
			
					
			totalAmbient2 = mambient     * lambient;
			totalDiffuse2 = NdotL   * mdiffuse  * ldiffuse; 
			totalSpecular2 =  mspecular * lspecular * (pow(NdotH, 4*specularPower));
			
			if(reflectorMode == 1){
				float spotEffect = dot(normalize(spotDirection), (-ld));
				float epsilon = spotCutOff - cutOff;
				float intensity = clamp((spotEffect - spotCutOff) / epsilon, 0.0, 1.0);	
				if (spotEffect > spotCutOff){	
					outColor = totalAmbient2 + intensity*att*(totalDiffuse2 + totalSpecular2);
				} else {
					outColor = totalAmbient2;
				}
			} else {
				outColor = totalAmbient2 + att * (totalDiffuse2 + totalSpecular2);
			}
			break;
		
			
	}	 								
} 
