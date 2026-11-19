package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_103_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swordplay", "2_103");

					// Frodo, Son of Drogo 1_290 is the Ring-bearer: a Hobbit who bears The Ruling Ring (of type The One Ring, no item) and, in the first test, a Pipe: no weapon.
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, vitality 4; his abilities are used only on purpose
					put("hobbitsword", "1_299"); // Hobbit Sword: [shire] possession, hand weapon; "Bearer must be a Hobbit."
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally, vitality 3; his ability is used only on purpose
					put("sting", "30_47");      // Sting (Hobbit set): [shire] ARTIFACT hand weapon on Bilbo, the one artifact weapon a Hobbit can bear; its trigger needs a skirmish won
					put("pipe", "1_292");       // The Gaffer's Pipe: [shire] possession, Pipe (no weapon) on a Hobbit; its Fellowship ability is not reached
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man; his ability is used only on purpose
					put("rangersword", "1_112"); // Ranger's Sword: hand weapon on Aragorn, damage +1

					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: [men] minion who is a Hobbit, vitality 4; his abilities are used only on purpose
					put("poleaxe", "11_95");    // Poleaxe: [men] hand weapon on a [men] minion; its skirmish ability is used only on purpose
					put("pavise", "11_94");     // Pavise: [men] shield on a [men] minion; "Bearer cannot take wounds except during a skirmish."
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1 (exhausted); cheated in, so its text does nothing
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HobbitSwordplayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Hobbit Sword-play
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert a Hobbit bearing a weapon to wound a minion. That minion's owner may remove (3) to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swordplay");

		assertEquals("Hobbit Sword-play", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAHobbitBearingAWeaponToWoundAMinionWhoseOwnerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordplay = scn.GetFreepsCard("swordplay");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sting = scn.GetFreepsCard("sting");
		var pipe = scn.GetFreepsCard("pipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(sam, hobbitsword);
		scn.AttachCardsTo(bilbo, sting);
		scn.AttachCardsTo(frodo, pipe);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.MoveCardsToHand(swordplay);

		var ted = scn.GetShadowCard("ted");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var runner = scn.GetShadowCard("runner");

		assertAttachedTo(hobbitsword, sam);
		assertAttachedTo(sting, bilbo);
		assertAttachedTo(pipe, frodo);
		assertAttachedTo(rangersword, aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(ted, runner);
		scn.AttachCardsTo(ted, poleaxe);
		assertAttachedTo(poleaxe, ted);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(3);

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(swordplay));

		scn.FreepsPlayCard(swordplay);
		// A companion with a possession, an ally with an artifact, and the Shadow player's Hobbit minion with a
		// Shadow weapon may pay; Frodo bears a Pipe and The Ruling Ring and no weapon, and Aragorn is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, bilbo, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(ted));

		// Minions of two cultures and races, the Runner exhausted; no Free Peoples character.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, bilbo, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);

		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();
		// The wound kills the exhausted Runner.
		assertInDiscard(runner, swordplay);
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(ted));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheOwnerRemovesThreeToPreventTheWoundAndTheExertionStaysPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordplay = scn.GetFreepsCard("swordplay");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sting = scn.GetFreepsCard("sting");
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(bilbo, sting);
		scn.MoveCardsToHand(swordplay);

		var ted = scn.GetShadowCard("ted");

		assertAttachedTo(sting, bilbo);
		scn.StartGame();
		scn.MoveMinionsToTable(ted);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(4);

		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(4, scn.GetTwilight());

		// Bilbo, the one Hobbit bearing a weapon, pays unasked; Ted is the one minion.
		scn.FreepsPlayCard(swordplay);
		assertEquals(1, scn.GetWoundsOn(bilbo));

		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertInDiscard(swordplay);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyHobbitBearingAWeaponIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordplay = scn.GetFreepsCard("swordplay");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, hobbitsword);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.MoveCardsToHand(swordplay);

		var runner = scn.GetShadowCard("runner");

		assertAttachedTo(hobbitsword, sam);
		assertAttachedTo(rangersword, aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		// Vitality 4 with 3 wounds: Sam cannot exert.  Frodo (no weapon) and Aragorn (no Hobbit) could.
		scn.AddWoundsToChar(sam, 3);
		assertEquals(3, scn.GetWoundsOn(sam));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(swordplay));
	}

	@Test
	public void WithFewerThanThreeTwilightTheOwnerIsNotAskedAndTheMinionIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordplay = scn.GetFreepsCard("swordplay");
		var sam = scn.GetFreepsCard("sam");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, hobbitsword);
		scn.MoveCardsToHand(swordplay);

		var ted = scn.GetShadowCard("ted");

		assertAttachedTo(hobbitsword, sam);
		scn.StartGame();
		scn.MoveMinionsToTable(ted);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);

		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(2, scn.GetTwilight());

		scn.FreepsPlayCard(swordplay);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AMinionThatCannotTakeWoundsIsNotWoundedAndItsOwnerIsNotAskedToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordplay = scn.GetFreepsCard("swordplay");
		var sam = scn.GetFreepsCard("sam");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, hobbitsword);
		scn.MoveCardsToHand(swordplay);

		var ted = scn.GetShadowCard("ted");
		var pavise = scn.GetShadowCard("pavise");

		assertAttachedTo(hobbitsword, sam);
		scn.StartGame();
		scn.MoveMinionsToTable(ted);
		scn.AttachCardsTo(ted, pavise);
		assertAttachedTo(pavise, ted);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(3);

		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(3, scn.GetTwilight());

		// Ted, under Pavise, cannot take wounds outside a skirmish: the wound will not happen.
		scn.FreepsPlayCard(swordplay);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(swordplay);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
