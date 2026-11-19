package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_064_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nasty1", "7_64");
					put("nasty2", "7_64");
					// Sméagol, Poor Creature: the [gollum] Sméagol, strength 3; a skirmish ability used only on purpose
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: the [shire] Sméagol, strength 3; an assignment ability used only on purpose
					put("smeagol2", "103_116");
					// Aragorn, Ranger of the North: a [gondor] companion, strength 8; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Don't Look at Them: a [gollum] Free Peoples condition; a skirmish ability used only on purpose
					put("look1", "6_39");
					put("look2", "6_39");
					// Gollum, Nasty Treacherous Creature: a [gollum] minion, strength 5; a regroup ability only
					put("gollum", "5_24");
					// Fat One Wants It: a [gollum] Shadow condition; its trigger answers a play of Gollum or Sméagol, and
					// both are cheated in
					put("fatone", "7_57");
					// Goblin Runner: a [moria] minion, strength 5; a play trigger only
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NastyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Nasty
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make Sméagol strength +1 for each [gollum] card you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nasty1");

		assertEquals("Nasty", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nasty1 = scn.GetFreepsCard("nasty1");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(nasty1);
		assertInHand(nasty1);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase next door to the skirmish, with Sméagol and two [gollum] cards there: not playable.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(nasty1));
	}

	@Test
	public void MakesSmeagolStrongerByTheNumberOfGollumCardsNamedForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nasty1 = scn.GetFreepsCard("nasty1");
		var nasty2 = scn.GetFreepsCard("nasty2");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var look1 = scn.GetFreepsCard("look1");
		var look2 = scn.GetFreepsCard("look2");
		var gollum = scn.GetShadowCard("gollum");
		var fatone = scn.GetShadowCard("fatone");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		// A [gollum] card in play of each side and of three card types: Sméagol, Don't Look at Them, and the
		// Shadow player's Gollum and Fat One Wants It.  A [gollum] card in the discard pile and the second Nasty,
		// in hand, are not in play.
		scn.MoveCardsToSupportArea(look1, fatone);
		scn.MoveCardsToDiscard(look2);
		scn.MoveCardsToHand(nasty1, nasty2);
		assertInZone(Zone.SUPPORT, look1, fatone);
		assertInZone(Zone.DISCARD, look2);
		assertInHand(nasty1, nasty2);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, runner);
		scn.SkipToAssignments();
		// Aragorn's skirmish first; Sméagol's, the next phase, still to come.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{smeagol, gollum});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsPlayAvailable(nasty1));
		scn.FreepsPlayCard(nasty1);

		// Nothing is added before the player names how many [gollum] cards he spots (CR 4.2 "spot").
		assertEquals(3, scn.GetStrength(smeagol));
		// Four [gollum] cards in play; not Frodo, Aragorn or the Goblin Runner, nor the cards out of play.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		// The number comes filled in with all four (F-0561), and fewer may be named.
		assertEquals(Integer.valueOf(4), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("2");

		assertInDiscard(nasty1);
		// Two named of the four: 3 + 2, not + 4.  Sméagol is not in this skirmish, and is made stronger all the
		// same; Aragorn, the companion who is in it, is not.
		assertEquals(5, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.ShadowPass();
		scn.FreepsPlayCard(nasty2);
		assertEquals(5, scn.GetStrength(smeagol));
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("4");

		assertInDiscard(nasty2);
		// All four named: 5 + 4.
		assertEquals(9, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn beats the Runner; that skirmish was the phase the bonuses lasted for.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(smeagol);
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, gollum));
		assertEquals(3, scn.GetStrength(smeagol));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SmeagolOnceLikeUsIsMadeStrongerAndIsNotCountedHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nasty1 = scn.GetFreepsCard("nasty1");
		var smeagol2 = scn.GetFreepsCard("smeagol2");
		var look1 = scn.GetFreepsCard("look1");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(smeagol2);
		scn.MoveCardsToSupportArea(look1);
		scn.MoveCardsToHand(nasty1);
		assertInZone(Zone.SUPPORT, look1);
		assertInHand(nasty1);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol2, gollum);
		scn.FreepsResolveSkirmish(smeagol2);

		assertEquals(3, scn.GetStrength(smeagol2));
		scn.FreepsPlayCard(nasty1);
		// Gollum and Don't Look at Them: the [shire] Sméagol is no [gollum] card.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		assertEquals(5, scn.GetStrength(smeagol2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoSmeagolInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nasty1 = scn.GetFreepsCard("nasty1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var look1 = scn.GetFreepsCard("look1");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(look1);
		scn.MoveCardsToHand(nasty1);
		assertInZone(Zone.SUPPORT, look1);
		assertInHand(nasty1);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);

		// An event's target is no requirement: it may be played with no Sméagol to make stronger.
		assertEquals(8, scn.GetStrength(aragorn));
		// Frodo, 3 and the Ruling Ring's +1.
		assertEquals(4, scn.GetStrength(scn.GetRingBearer()));
		assertTrue(scn.FreepsPlayAvailable(nasty1));
		scn.FreepsPlayCard(nasty1);
		scn.FreepsChoose("2");

		assertInDiscard(nasty1);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(scn.GetRingBearer()));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
