package com.gempukku.lotro.cards.official.set14;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_14_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hillman", "14_10");
					put("hillman2", "14_10");
					put("legion", "11_88");     // Legion of Harad: [men] Man minion; toil 2 is its whole text, and it is cheated in
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability used only on purpose
					// Noble Intentions: [shire] Skirmish event, "Exert a companion (except a Hobbit) to make a Hobbit strength +3"
					put("noble", "1_304");
					// Disquiet of Our People: [dwarven] Maneuver event, "Exert a Dwarf to make that Dwarf defender +1 (or +2 if you spot an Orc)"
					put("disquiet", "2_2");
					// Eowyn, Daughter of Eomund: [rohan] companion, "Maneuver: Play a [rohan] possession on Eowyn to heal her"
					put("eowyn", "5_122");
					put("spear", "4_288");      // Rider's Spear: [rohan] hand weapon on a [rohan] Man, cost 1
					// Fires Brightly Burning: [men] Shadow Skirmish event, "Make your [men] minion strength +2"; its
					// alternative cost wants 4 [men] cards in the discard pile, and there are none
					put("fires", "13_91");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Hillman beside a [men] minion who is no Man, with a [men] Man to put down later.
	protected VirtualTableScenario GetAnotherMenManScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hillman", "14_10");
					put("ted", "18_75");
					put("corps", "11_73");
					put("gimli", "1_13");
					put("disquiet1", "2_2");
					put("disquiet2", "2_2");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FuriousHillmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Furious Hillman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Response:</b> If the Free Peoples player plays an event, spot another [men] Man and discard this minion from play to cancel that event.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hillman");

		assertEquals("Furious Hillman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// "spot another [men] Man": the definition spotted any other [men] card.
	@Test
	public void TheResponseWantsAnotherMenManAndNotJustAnotherMenCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAnotherMenManScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var disquiet1 = scn.GetFreepsCard("disquiet1");
		var disquiet2 = scn.GetFreepsCard("disquiet2");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(disquiet1, disquiet2);

		var hillman = scn.GetShadowCard("hillman");
		var ted = scn.GetShadowCard("ted");
		var corps = scn.GetShadowCard("corps");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman, ted);
		scn.SkipToPhase(Phase.MANEUVER);

		// Ted Sandyman is a [men] minion and a Hobbit: there is no other [men] Man, and the event goes through.
		scn.FreepsPlayCard(disquiet1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// With a [men] Man beside him the Hillman may answer the next event.
		scn.MoveMinionsToTable(corps);
		scn.ShadowPass();
		scn.FreepsPlayCard(disquiet2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, hillman);
		// Cancelled: Gimli exerted for it and is no more of a defender than he was.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void EitherOfTwoHillmenCancelsASkirmishEventAndOnlyTheOneUsedIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var noble = scn.GetFreepsCard("noble");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(noble);

		var hillman = scn.GetShadowCard("hillman");
		var hillman2 = scn.GetShadowCard("hillman2");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman, hillman2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, hillman}, new PhysicalCardImpl[]{aragorn, hillman2});
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Frodo 3, +1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPlayCard(noble);
		// Aragorn, the one companion who is no Hobbit, exerts for it.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Each Hillman spots the other as another [men] Man.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hillman));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hillman2));

		scn.ShadowChooseOptionalTrigger(hillman2);
		assertInDiscard(hillman2);
		assertInZone(Zone.SHADOW_CHARACTERS, hillman);
		// Cancelled: the cost stays paid, the strength is not given and the event is discarded.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetStrength(frodo));
		assertInDiscard(noble);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ADunlandManIsNoMenManToSpotAndTheEventIsCarriedOut() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var disquiet = scn.GetFreepsCard("disquiet");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(disquiet);

		var hillman = scn.GetShadowCard("hillman");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman, brigand);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPlayCard(disquiet);
		// No response window: the Shadow player is straight on to his maneuver actions.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hillman));
		assertEquals(1, scn.GetWoundsOn(gimli));
		// No Orc to spot: defender +1
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertInZone(Zone.SHADOW_CHARACTERS, hillman);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void APossessionTheFreePeoplesPlayerPlaysIsNoEventAndIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToHand(spear);

		var hillman = scn.GetShadowCard("hillman");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		scn.AddWoundsToChar(eowyn, 1);
		scn.MoveMinionsToTable(hillman, legion);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertInHand(spear);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Her ability plays the one [rohan] possession in hand on her: a Free Peoples card played that is no event.
		scn.FreepsUseCardAction(eowyn);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hillman));
		assertAttachedTo(spear, eowyn);
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertInZone(Zone.SHADOW_CHARACTERS, hillman);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AShadowEventIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hillman = scn.GetShadowCard("hillman");
		var legion = scn.GetShadowCard("legion");
		var fires = scn.GetShadowCard("fires");
		scn.MoveCardsToHand(fires);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(hillman, legion);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, legion}, new PhysicalCardImpl[]{aragorn, hillman});
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		assertEquals(14, scn.GetStrength(legion));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPlayCard(fires);
		// Your [men] minion: the Legion or the Hillman.
		scn.ShadowChooseCard(legion);
		// No response: the Free Peoples player is asked for his skirmish actions.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hillman));
		assertEquals(16, scn.GetStrength(legion));
		assertInZone(Zone.SHADOW_CHARACTERS, hillman);
		assertInDiscard(fires);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
