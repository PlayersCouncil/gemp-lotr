package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chieftain", "15_99");
					put("goblin", "11_137");     // Scurrying Goblin: an [orc] Orc; its own offer comes only when it is played underground
					put("halftroll", "14_12");   // Half-troll of Far Harad: an [orc] Half-troll; its start-of-assignment trigger reaches no test here
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc minion, no text
					put("beckoning", "12_83");   // The Beckoning Shadow: [orc] support-area condition; nothing below 5 burdens
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability used only on purpose
					put("sword", "1_112");       // Ranger's Sword: [gondor] possession borne by Aragorn
					put("toby", "1_305");        // Old Toby: [shire] possession in the support area; its draw is on its own play only
					put("shards", "3_44");       // The Shards of Narsil: [gondor] artifact in the support area; fellowship abilities only
					put("phial", "10_13");       // Phial of Galadriel, Star-glass: [elven] artifact borne by a Hobbit; a skirmish ability only
					put("lordofmoria", "1_21");  // Lord of Moria: a Free Peoples condition that is neither a possession nor an artifact
					put("poison", "8_28");       // Spider Poison: a Shadow possession in the support area
					put("ithil", "9_47");        // Ithil Stone: a Shadow artifact in the support area; an optional draw at the start of the Shadow phase
					put("boromir", "1_97");      // Boromir, Son of Denethor: a companion; a skirmish ability only
					put("gimli", "1_13");        // Gimli, Son of Gloin: a companion; a skirmish ability only
					put("sam", "1_311");         // Sam, Son of Hamfast: a companion; a fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetThreatScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chieftain", "15_99");
					put("goblin", "11_137");    // Scurrying Goblin: the [orc] minion to spot; its own offer comes only when it is played underground
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples possession, on a Dwarf
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackLandChieftainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Black Land Chieftain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: To play, spot an [orc] minion.<br>When you play Black Land Chieftain, you may add a threat for each Free Peoples possession and each Free Peoples artifact you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("chieftain");

		assertEquals("Black Land Chieftain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered.  "A threat for each
	// Free Peoples possession and each Free Peoples artifact" is no threat where there is neither.
	@Test
	public void TheThreatsAreNotOfferedWithNoFreePeoplesPossessionOrArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var chieftain = scn.GetShadowCard("chieftain");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToHand(chieftain);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Two companions, so a threat could be added; The One Ring is neither a possession nor an artifact.
		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheThreatsAreOfferedWithAFreePeoplesPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);

		var chieftain = scn.GetShadowCard("chieftain");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToHand(chieftain);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(chieftain);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheThreatsAreOfferedWithAFreePeoplesArtifactAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shards = scn.GetFreepsCard("shards");
		scn.MoveCardsToSupportArea(shards);

		var chieftain = scn.GetShadowCard("chieftain");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToHand(chieftain);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, shards);
		scn.MoveMinionsToTable(goblin);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// No possession anywhere: the artifact alone is spotted.
		scn.ShadowPlayCard(chieftain);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnOrcMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain = scn.GetShadowCard("chieftain");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToSupportArea(beckoning);
		scn.MoveCardsToHand(chieftain, halftroll);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beckoning);
		assertInHand(chieftain, halftroll);
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// An Orc minion of another culture, and an [orc] card that is no minion.
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(chieftain));

		scn.ShadowPlayCard(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertTrue(scn.ShadowPlayAvailable(chieftain));

		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		// No Free Peoples possession or artifact: no threat is offered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AddsAThreatForEachFreePeoplesPossessionAndEachFreePeoplesArtifactWithoutAskingHowMany() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var toby = scn.GetFreepsCard("toby");
		var shards = scn.GetFreepsCard("shards");
		var phial = scn.GetFreepsCard("phial");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("sam"));
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(frodo, phial);
		scn.MoveCardsToSupportArea(toby, shards, lordofmoria);

		var chieftain = scn.GetShadowCard("chieftain");
		var goblin = scn.GetShadowCard("goblin");
		var poison = scn.GetShadowCard("poison");
		var ithil = scn.GetShadowCard("ithil");
		scn.MoveCardsToHand(chieftain);
		scn.MoveCardsToSupportArea(poison);

		scn.StartGame();
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(phial, frodo);
		assertInZone(Zone.SUPPORT, toby, shards, lordofmoria, poison);
		scn.MoveMinionsToTable(goblin);
		scn.SetTwilight(20);
		scn.FreepsPass();
		// Laid after its own start-of-Shadow-phase draw would have been asked.
		scn.MoveCardsToSupportArea(ithil);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, ithil);
		assertEquals(0, scn.GetThreats());

		scn.ShadowPlayCard(chieftain);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		scn.ShadowAcceptOptionalTrigger();
		// Two possessions (borne and in a support area) and two artifacts (the same), and nothing for the
		// Free Peoples condition, the Shadow possession, the Shadow artifact or The One Ring.  Five companions
		// leave room for more, and "you can spot" asks no number.
		assertEquals(4, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningAddsNoThreatAndAnotherOrcMinionPlayedAfterItOffersNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(toby);

		var chieftain = scn.GetShadowCard("chieftain");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToHand(chieftain, halftroll);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, toby);
		scn.MoveMinionsToTable(goblin);
		scn.SetTwilight(20);
		scn.FreepsPass();
		assertEquals(0, scn.GetThreats());

		scn.ShadowPlayCard(chieftain);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
