#version 150
in vec3 position;
in vec3 normal;
in vec3 transNormal;
in vec3 lightDirection;
in vec3 ldForReflect;
in vec3 viewDirection;
in vec3 spotDirection;
in vec2 textCoords;
in float dist;
out vec4 outColor; 
uniform int lightMode;
uniform int reflectorMode;
uniform sampler2D tex;
uniform sampler2D texNormal;
uniform sampler2D texHeight;
vec3 ld;
vec3 ldR;
vec3 nd;
vec3 vd;
float NdotL;
vec4 totalAmbient; 
vec4 totalDiffuse;  
vec4 totalSpecular;
void main() {

					//material 
	vec4 mambient = vec4(1.0,0.7,0.7, 1.0); 
	vec4 mdiffuse = vec4(0.7,0.7,0.7, 1.0); 
	vec4 mspecular = vec4(1.0,1.0,1.0, 1.0); 
					//svìtlo 
	vec4 lambient = vec4(0.2,0.2,0.2, 1.0); 
	vec4 ldiffuse = vec4(0.8,0.8,0.8, 1.0); 
	vec4 lspecular = vec4(1.0,1.0,1.0, 1.0);
	
					//utlum svetla
	float constantAttenuation = 1.0;
	float linearAttenuation = 0.001;
	//float quadraticAttenuation = 0.001;
	float quadraticAttenuation = 0.0008;
	float att = 1.0 / (constantAttenuation + linearAttenuation*dist + quadraticAttenuation*dist*dist);
	
					// charakteristiky zrcadlove slozky a reflektoru
	float specularPower = 32.0;			
	float spotCutOff = 0.95; // 0.99
	float cutOff = 0.91;
	
	switch(lightMode){
		case 1: //Position			
			outColor = vec4(normalize(position.xyz), 1.0);						
			break;
		case 2: //Color			
			vec3 pos = normalize(position.xyz);					
			outColor = vec4((pos.x+1)/2,(pos.y+1)/2, (pos.z+1)/2, 1.0);			// prevest z <-1, 1> na <0, 1>
			break;
		case 3: //Normal			
			outColor = vec4(normalize(normal), 1.0);
			break;
		case 4:	//Diffuse			
			NdotL = max(dot(normalize(lightDirection),normalize(transNormal)),0.0);
			float diff = NdotL;
			outColor = diff * vec4(1.0, 1.0, 1.0, 1.0);
			break;
		case 5: // Blinn-Phong - Half vector
			ld = normalize(lightDirection); 
			nd = normalize(transNormal);
			vd = normalize(viewDirection);	
			NdotL = max(dot(ld,nd),0.0);
			
			vec3 halfVector = normalize( ld + vd);
			float NdotH = max(0.0 , dot( nd, halfVector));						 					
			
			totalAmbient = mambient * lambient;
			totalDiffuse = NdotL * mdiffuse * ldiffuse; 
			totalSpecular =  mspecular * lspecular * (pow(NdotH, 4.0*specularPower));
			
			if(reflectorMode == 1){
				float spotEffect = dot(normalize(spotDirection), (-ld));				
				float epsilon = spotCutOff - cutOff;
				float intensity = clamp((spotEffect - spotCutOff) / epsilon, 0.0, 1.0);					
				if (spotEffect > spotCutOff){	
					outColor = totalAmbient + intensity * att * (totalDiffuse + totalSpecular);
				} else {
					outColor = totalAmbient;
				}
			} else {
				outColor = totalAmbient + att * (totalDiffuse + totalSpecular);
			}
			break;		
		case 6: // Blinn-Phong - Reflect vector
			ld = normalize(lightDirection); 
			nd = normalize(transNormal);
			vd = normalize(viewDirection);	
			NdotL = max(dot(ld, nd),0.0);
			
			//vec3 reflection=reflect(-ld, nd);			
			//vec3 reflection=normalize((( 2.0 * nd) * NdotL) - ld);
			vec3 reflection=normalize((( 2.0 * dot(ld, nd) * nd)) - ld);						
			float RdotV = max(0.0 , dot( vd, reflection));			
			
			totalAmbient = mambient * lambient;
			totalDiffuse = NdotL * mdiffuse * ldiffuse; 
			totalSpecular =  mspecular * lspecular * (pow(RdotV, specularPower));
			
			if(reflectorMode == 1){
				float spotEffect = dot(normalize(spotDirection), (-ld));				
				float epsilon = spotCutOff - cutOff;
				float intensity = clamp((spotEffect - spotCutOff) / epsilon, 0.0, 1.0);					
				if (spotEffect > spotCutOff){	
					outColor = totalAmbient + intensity * att * (totalDiffuse + totalSpecular);
				} else {
					outColor = totalAmbient;
				}
			} else {
				outColor = totalAmbient + att * (totalDiffuse + totalSpecular);
			}
			break;
		case 7: // Normal Mapping				
			vec3 lnormal = normalize(texture2D( texNormal, textCoords).xyz * 2.0 - 1.0);
			vec4 color_base = texture2D(tex, textCoords);			
			
			ld = normalize(lightDirection);
			ldR = normalize(ldForReflect); 			
			vd = normalize(viewDirection);						
				
			NdotL = max(dot(ld,lnormal),0.0);
			
			halfVector = normalize( ld + vd);
			NdotH = max(0.0 , dot( lnormal, halfVector));						 											
			
			totalAmbient = lambient * color_base;
			totalDiffuse = NdotL * ldiffuse * color_base; 
			totalSpecular =  lspecular * (pow(NdotH, 20.0));
			
			if(reflectorMode == 1){
				float spotEffect = dot(normalize(spotDirection), (-ldR));				
				float epsilon = spotCutOff - cutOff;
				float intensity = clamp((spotEffect - spotCutOff) / epsilon, 0.0, 1.0);					
				if (spotEffect > spotCutOff){	
					outColor = totalAmbient + intensity * att * (totalDiffuse + totalSpecular);
				} else {
					outColor = totalAmbient;
				}
			} else {
				outColor = totalAmbient + att * (totalDiffuse + totalSpecular);
			}			
			break;
		case 8: // Parallax mapping			
			ld = normalize(lightDirection); 			
			vd = normalize(viewDirection);
			ldR = normalize(ldForReflect);
			
			vec2 cBumbSize = vec2(0.04, -0.02);						
			cBumbSize = vec2(0.02, -0.02);			
			float height = texture2D(texHeight, textCoords).r;
			height = height * cBumbSize.x + cBumbSize.y;
			
			vec2 texUV = textCoords.xy + vd.xy * height;
			vec4 color_base2 = texture2D(tex, texUV); 
			vec3 lnormal2 = normalize(texture2D( texNormal, texUV).xyz * 2.0 - 1.0);
																		
				
			NdotL = max(dot(ld,lnormal2),0.0);
			
			halfVector = normalize( ld + vd);
			NdotH = max(0.0 , dot( lnormal2, halfVector));						 					
			
			totalAmbient = lambient * color_base2;
			totalDiffuse = NdotL * ldiffuse * color_base2; 
			totalSpecular =  lspecular * (pow(NdotH, 30.0));
			
			if(reflectorMode == 1){
				float spotEffect = dot(normalize(spotDirection), (-ldR));				
				float epsilon = spotCutOff - cutOff;
				float intensity = clamp((spotEffect - spotCutOff) / epsilon, 0.0, 1.0);					
				if (spotEffect > spotCutOff){	
					outColor = totalAmbient + intensity * att * (totalDiffuse + totalSpecular);
				} else {
					outColor = totalAmbient;
				}
			} else {
				outColor = totalAmbient + att * (totalDiffuse + totalSpecular);
			}
			break;
	}
		
		
	
} 
