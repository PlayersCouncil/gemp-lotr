package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_194_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "15_18");    // Legolas, Fleet-footed Hunter: [elven] Elf companion, Hunter 1; a regroup draw never used here
					put("gimli", "15_5");       // Gimli, Eager Hunter: [dwarven] Dwarf companion, Damage +1, Hunter 3, no other text
					put("aragorn", "4_109");    // Aragorn, Heir of Elendil: [gondor] Man companion who is no hunter; Defender +1 only
					put("rohanaragorn", "17_93");  // Aragorn, Defender of Rohan: [rohan] companion who gains hunter 1 while the Ring-bearer is assigned
					put("uruk", "15_171");      // Tracking Uruk: [uruk-hai] Uruk-hai minion, Hunter 1 and nothing else
					put("breeder", "18_87");    // Orkish Breeder: [orc] Orc minion, Hunter 1; a Shadow ability never used here
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion who is no hunter; no text
				}},
				new HashMap<>()
				{{
					// An unordered Shadows-block path, with Westfold Village in the place of Fangorn Glade.
					putAll(VirtualTableScenario.ShadowsSites);
					put("site1", "15_194");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WestfoldVillageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Westfold Village
		 * Unique: False
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Left
		 * Type: Site
		 * Game Text: <b>Plains</b>. Each hunter character is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("site1");

		assertEquals("Westfold Village", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void EachHunterCompanionAndHunterMinionIsStrengthPlus1AndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var uruk = scn.GetShadowCard("uruk");
		var breeder = scn.GetShadowCard("breeder");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas, gimli, aragorn);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.MoveMinionsToTable(uruk, breeder, troop);

		assertEquals("Westfold Village", scn.GetCurrentSite().getBlueprint().getTitle());
		// Hunters of two cultures and races on each side, read outside any skirmish (where a hunter's own bonus
		// would reach them): each is its printed strength +1.
		assertEquals(7, scn.GetStrength(legolas));   // 6 + 1
		assertEquals(7, scn.GetStrength(gimli));     // 6 + 1
		assertEquals(15, scn.GetStrength(uruk));     // 14 + 1
		assertEquals(10, scn.GetStrength(breeder));  // 9 + 1
		// Characters who are no hunters, a companion, the Ring-bearer and a minion, are at their printed strength.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));     // 3 + 1 for The Ruling Ring
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACharacterThatGainsHunterAtWestfoldVillageGainsTheBonusWithIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("rohanaragorn");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame(scn.GetFreepsSite("site2"));
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();
		// The Shadow player lays his own copy of Westfold Village as the fellowship's next site.
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site1"));
		assertEquals("Westfold Village", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.SkipToAssignments();
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.HUNTER));
		assertEquals(8, scn.GetStrength(aragorn));

		// With the Ring-bearer assigned, the [rohan] Aragorn gains hunter 1, and the site's +1 with it. He is in
		// no skirmish, so his own hunter bonus does not reach him.
		scn.FreepsAssignToMinions(frodo, troop);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.HUNTER));
		assertEquals(9, scn.GetStrength(aragorn));   // 8 + 1
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void OnceTheFellowshipHasLeftWestfoldVillageHuntersAreAtTheirPrintedStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var uruk = scn.GetShadowCard("uruk");
		var breeder = scn.GetShadowCard("breeder");
		scn.MoveCompanionsToTable(legolas, gimli);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.MoveMinionsToTable(uruk, breeder);

		assertEquals(7, scn.GetStrength(legolas));
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(15, scn.GetStrength(uruk));
		assertEquals(10, scn.GetStrength(breeder));

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		assertEquals("Abandoned Mine Shaft", scn.GetCurrentSite().getBlueprint().getTitle());

		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(14, scn.GetStrength(uruk));
		assertEquals(9, scn.GetStrength(breeder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
