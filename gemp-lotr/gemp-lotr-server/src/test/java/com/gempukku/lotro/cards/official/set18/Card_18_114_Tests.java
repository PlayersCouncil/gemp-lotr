package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cleaved1", "18_114");
					put("cleaved2", "18_114");

					// [uruk-hai] hunters
					put("tracking", "15_171");  // Tracking Uruk: Uruk-hai, vitality 3; Hunter 1 is its whole text
					put("whguard", "17_126");   // White Hand Guard: Uruk-hai, vitality 3, fierce, hunter 1; a skirmish ability never used here
					put("butcher", "17_122");   // White Hand Butcher: Uruk-hai, vitality 2; Hunter 6 is its whole text
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is no hunter (a Wizard), vitality 4;
					// each companion is resistance -1, and a skirmish ability never used here
					put("saruman", "17_116");
					// Grishnákh, Treacherous Captain: an [orc] hunter, vitality 3; a Shadow-phase ability never used here
					put("grishnakh", "17_71");
					put("cuguard", "10_81");    // Cirith Ungol Guard: a [sauron] Uruk-hai; Damage +1 is its whole text
					put("marksman", "1_176");   // Goblin Marksman: a Shadow archer; Archer is its whole text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man; a maneuver ability never used here
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound [dwarven] Dwarf; a skirmish ability never used here
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer; his abilities are never used
					put("albert", "1_69");      // Albert Dreary, Entertainer from Bree: an ally, no companion; a maneuver ability never used here
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an archer companion; +3 only against a Nazgûl
					put("dinendal", "3_12");    // Dínendal, Silent Scout: an archer ally; a regroup ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CleavedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Cleaved
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Vitality: -1
		 * Game Text: <b>Regroup:</b> Exert an [uruk-hai] hunter twice to transfer this condition from your support area to an unbound companion.<br><b>Archery:</b> Spot an [uruk-hai] minion to transfer this condition from your support area to a Free Peoples archer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cleaved1");

		assertEquals("Cleaved", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(-1, card.getBlueprint().getVitality());
	}

	@Test
	public void RegroupExertingAnUrukHaiHunterTwiceTransfersTheCopyUsedToTheUnboundCompanionChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var albert = scn.GetFreepsCard("albert");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(albert);

		var cleaved1 = scn.GetShadowCard("cleaved1");
		var cleaved2 = scn.GetShadowCard("cleaved2");
		var tracking = scn.GetShadowCard("tracking");
		var whguard = scn.GetShadowCard("whguard");
		var saruman = scn.GetShadowCard("saruman");
		var grishnakh = scn.GetShadowCard("grishnakh");
		scn.MoveCardsToSupportArea(cleaved1, cleaved2);
		assertInZone(Zone.SUPPORT, cleaved1, cleaved2);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking, whguard, saruman, grishnakh);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(tracking));
		assertEquals(0, scn.GetWoundsOn(whguard));
		assertTrue(scn.ShadowActionAvailable(cleaved1));
		scn.ShadowUseCardAction(cleaved1);

		// Who exerts: an [uruk-hai] hunter. Saruman is [uruk-hai] and no hunter, Grishnákh a hunter of the [orc]
		// culture; each could exert twice.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tracking, whguard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, grishnakh));
		scn.ShadowChooseCard(tracking);
		assertEquals(2, scn.GetWoundsOn(tracking));
		assertEquals(0, scn.GetWoundsOn(whguard));

		// Where to: an unbound companion. Not the Ring-bearer, not Ring-bound Sam, not an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, albert));
		scn.ShadowChooseCard(gimli);

		assertAttachedTo(cleaved1, gimli);
		assertInZone(Zone.SUPPORT, cleaved2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionTheHunterExertsTwiceAndTheConditionStaysInTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var albert = scn.GetFreepsCard("albert");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(albert);

		var cleaved1 = scn.GetShadowCard("cleaved1");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCardsToSupportArea(cleaved1);
		assertInZone(Zone.SUPPORT, cleaved1);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// Frodo, Sam and an ally: nobody it could go to. The ability has a cost, and is offered all the same.
		assertEquals(0, scn.GetWoundsOn(tracking));
		assertTrue(scn.ShadowActionAvailable(cleaved1));
		scn.ShadowUseCardAction(cleaved1);

		assertEquals(2, scn.GetWoundsOn(tracking));
		assertInZone(Zone.SUPPORT, cleaved1);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhileNoUrukHaiHunterCanExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var cleaved1 = scn.GetShadowCard("cleaved1");
		var butcher = scn.GetShadowCard("butcher");
		var tracking = scn.GetShadowCard("tracking");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(cleaved1);
		assertInZone(Zone.SUPPORT, cleaved1);

		scn.StartGame();
		scn.MoveMinionsToTable(butcher, tracking, saruman);
		scn.AddWoundsToChar(tracking, 1);
		assertEquals(1, scn.GetWoundsOn(tracking));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// The White Hand Butcher (vitality 2) and the Tracking Uruk with a wound (2 left) can each exert once and
		// not twice; Saruman could exert twice and is no hunter.
		assertEquals(2, scn.GetVitality(butcher));
		assertEquals(2, scn.GetVitality(tracking));
		assertEquals(4, scn.GetVitality(saruman));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cleaved1));
		assertInZone(Zone.SUPPORT, cleaved1);
	}

	@Test
	public void ACopyBorneByACompanionHasNoRegroupAbilityWhileOneInTheSupportAreaHas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var cleaved1 = scn.GetShadowCard("cleaved1");
		var cleaved2 = scn.GetShadowCard("cleaved2");
		var tracking = scn.GetShadowCard("tracking");
		scn.AttachCardsTo(aragorn, cleaved1);
		scn.MoveCardsToSupportArea(cleaved2);
		assertAttachedTo(cleaved1, aragorn);
		assertInZone(Zone.SUPPORT, cleaved2);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// The Tracking Uruk can pay, and Gimli could take either copy.
		assertEquals(0, scn.GetWoundsOn(tracking));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(cleaved2));
		assertFalse(scn.ShadowActionAvailable(cleaved1));
	}

	@Test
	public void ArcherySpottingAnUrukHaiCultureMinionTheCopyUsedGoesToTheFreePeoplesArcherChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dinendal = scn.GetFreepsCard("dinendal");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveAlliesToTable(dinendal);

		var cleaved1 = scn.GetShadowCard("cleaved1");
		var cleaved2 = scn.GetShadowCard("cleaved2");
		var saruman = scn.GetShadowCard("saruman");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToSupportArea(cleaved1, cleaved2);
		assertInZone(Zone.SUPPORT, cleaved1, cleaved2);

		scn.StartGame();
		// Saruman is the only [uruk-hai] minion: an [uruk-hai] Wizard, no Uruk-hai.
		scn.MoveMinionsToTable(saruman, marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(cleaved1));
		scn.ShadowUseCardAction(cleaved1);

		// A Free Peoples archer: a companion or an ally. Not a companion who is no archer, not the Shadow archer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(legolas, dinendal));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo, marksman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(dinendal);

		assertAttachedTo(cleaved1, dinendal);
		assertInZone(Zone.SUPPORT, cleaved2);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityNotOfferedWithOnlyAnUrukHaiOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var cleaved1 = scn.GetShadowCard("cleaved1");
		var cuguard = scn.GetShadowCard("cuguard");
		scn.MoveCardsToSupportArea(cleaved1);
		assertInZone(Zone.SUPPORT, cleaved1);

		scn.StartGame();
		// A [sauron] Uruk-hai; the [uruk-hai] card in play that is no minion is Cleaved itself.
		scn.MoveMinionsToTable(cuguard);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cleaved1));
		assertInZone(Zone.SUPPORT, cleaved1);
	}

	@Test
	public void ArcheryAbilityNotOfferedWithNoFreePeoplesArcherToTakeIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var cleaved1 = scn.GetShadowCard("cleaved1");
		var tracking = scn.GetShadowCard("tracking");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToSupportArea(cleaved1);
		assertInZone(Zone.SUPPORT, cleaved1);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking, marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// An [uruk-hai] minion is there to spot, and the only archer is the Shadow player's: the ability costs
		// nothing and would do nothing, and is not offered.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cleaved1));
		assertInZone(Zone.SUPPORT, cleaved1);
	}

	@Test
	public void ACopyBorneByAnArcherHasNoArcheryAbilityWhileOneInTheSupportAreaHas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var dinendal = scn.GetFreepsCard("dinendal");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(dinendal);

		var cleaved1 = scn.GetShadowCard("cleaved1");
		var cleaved2 = scn.GetShadowCard("cleaved2");
		var tracking = scn.GetShadowCard("tracking");
		scn.AttachCardsTo(legolas, cleaved1);
		scn.MoveCardsToSupportArea(cleaved2);
		assertAttachedTo(cleaved1, legolas);
		assertInZone(Zone.SUPPORT, cleaved2);

		scn.StartGame();
		// An [uruk-hai] Uruk-hai to spot, and Dínendal could take either copy.
		scn.MoveMinionsToTable(tracking);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(cleaved2));
		assertFalse(scn.ShadowActionAvailable(cleaved1));
	}
}
