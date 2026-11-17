package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("let1", "7_63");
					put("let2", "7_63");
					put("let3", "7_63");
					put("let4", "7_63");
					// Gollum, Nasty Treacherous Creature: the [gollum] Gollum, a minion of no race; a regroup ability only
					put("gollum", "5_24");
					// Gollum, Half a Wraith Himself: the [wraith] Gollum; a trigger on playing a Nazgûl and an
					// assignment ability, neither reached here
					put("gollum2", "103_66");
					// Sméagol, Poor Creature: the [gollum] Sméagol, a Free Peoples companion; a skirmish ability used only on purpose
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: the [shire] Sméagol; an assignment ability used only on purpose
					put("smeagol2", "103_116");
					// Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum; damage +1 only in a fierce skirmish
					put("shelob", "8_26");
					// Goblin Runner: a [moria] Orc, with no damage bonus; a play trigger only
					put("runner", "1_178");
					// Morgul Whelp: a [wraith] Orc; a standing strength bonus only
					put("whelp", "7_202");
					// In Twilight: another condition a minion bears ([wraith] minions); its play trigger is not reached,
					// as it is cheated on
					put("twilight", "19_34");
					// Cards for the Free Peoples hand: with 4 there he has initiative, with 3 the Shadow player has.
					put("strike", "1_3");       // Axe Strike
					put("strike2", "1_3");
					put("lorien", "1_53");      // Lórien Elf
					put("guard", "1_7");        // Dwarf Guard
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LetHerDealWithThemStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Let Her Deal With Them
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Game Text: To play, spot Gollum or Sméagol. Bearer must be a minion. Limit 1 per bearer.<br>Bearer is <b>damage +1</b>.<br>If you have initiative during the Shadow phase, you may play this condition from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("let1");

		assertEquals("Let Her Deal With Them", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilGollumIsSpottedAndThenGoesOnAMinionWhoIsDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var let1 = scn.GetShadowCard("let1");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(let1, gollum);
		assertInHand(let1, gollum);

		scn.StartGame();
		// A [gollum] minion who is no Gollum, and a minion of another culture.
		scn.MoveMinionsToTable(shelob, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(let1));

		scn.ShadowPlayCard(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.ShadowPlayAvailable(let1));

		assertEquals(0, scn.GetKeywordCount(runner, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(gollum, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(shelob, Keyword.DAMAGE));
		scn.ShadowPlayCard(let1);
		// Any minion, of any culture or race (Gollum has none); not a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, shelob, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(runner);
		assertAttachedTo(let1, runner);
		// The bearer is damage +1; the minions beside him are not.
		assertEquals(1, scn.GetKeywordCount(runner, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(gollum, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(shelob, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheWraithGollumIsSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var let1 = scn.GetShadowCard("let1");
		var gollum2 = scn.GetShadowCard("gollum2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(let1);
		assertInHand(let1);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum2, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(let1));
		scn.ShadowPlayCard(let1);
		scn.ShadowChooseCard(runner);
		assertAttachedTo(let1, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesSmeagolIsSpottedToPlayItAndIsNoBearerForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var let1 = scn.GetShadowCard("let1");
		var smeagol = scn.GetFreepsCard("smeagol");
		var runner = scn.GetShadowCard("runner");
		var whelp = scn.GetShadowCard("whelp");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(let1);
		assertInHand(let1);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, whelp);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The other player's Sméagol, a companion, is spotted; no Gollum is in play.
		assertTrue(scn.ShadowPlayAvailable(let1));
		scn.ShadowPlayCard(let1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, whelp));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(smeagol, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(whelp);
		assertAttachedTo(let1, whelp);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SmeagolOnceLikeUsIsSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var let1 = scn.GetShadowCard("let1");
		var smeagol2 = scn.GetFreepsCard("smeagol2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol2);
		scn.MoveCardsToHand(let1);
		assertInHand(let1);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(let1));
		scn.ShadowPlayCard(let1);
		assertAttachedTo(let1, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionThatBearsACopyIsNotOfferedForAnotherAndWithEveryMinionBearingOneItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var let1 = scn.GetShadowCard("let1");
		var let2 = scn.GetShadowCard("let2");
		var let3 = scn.GetShadowCard("let3");
		var let4 = scn.GetShadowCard("let4");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var whelp = scn.GetShadowCard("whelp");
		var twilight = scn.GetShadowCard("twilight");
		scn.MoveCardsToHand(let2, let3, let4);
		assertInHand(let2, let3, let4);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, runner, whelp);
		// The Runner bears a copy; the Whelp bears another condition; Gollum bears nothing.
		scn.AttachCardsTo(runner, let1);
		scn.AttachCardsTo(whelp, twilight);
		assertAttachedTo(let1, runner);
		assertAttachedTo(twilight, whelp);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(let2));
		scn.ShadowPlayCard(let2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, whelp));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		scn.ShadowChooseCard(gollum);
		assertAttachedTo(let2, gollum);

		// The Whelp is the one minion left without a copy: it goes on him.
		assertTrue(scn.ShadowPlayAvailable(let3));
		scn.ShadowPlayCard(let3);
		assertAttachedTo(let3, whelp);
		assertAttachedTo(twilight, whelp);

		// Every minion bears one now: Gollum is still spotted and the pool can pay, but there is no bearer.
		assertEquals(1, scn.GetKeywordCount(whelp, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(let4));
	}

	@Test
	public void WithInitiativeInTheShadowPhaseItIsPlayedFromTheDiscardPileOntoAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var let1 = scn.GetShadowCard("let1");
		var twilight = scn.GetShadowCard("twilight");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToDiscard(let1, twilight);
		scn.MoveCardsToHand(scn.GetFreepsCard("strike"), scn.GetFreepsCard("lorien"), scn.GetFreepsCard("guard"));
		assertInZone(Zone.DISCARD, let1, twilight);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Three cards in the Free Peoples hand: the Shadow has initiative.
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertInZone(Zone.DISCARD, let1, twilight);
		assertTrue(scn.ShadowActionAvailable(let1));
		// Another condition in the same pile is not offered.
		assertFalse(scn.ShadowActionAvailable(twilight));

		assertEquals(0, scn.GetKeywordCount(runner, Keyword.DAMAGE));
		scn.ShadowUseCardAction(let1);
		// From the pile it is still played on a minion of the player's choice.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);

		assertAttachedTo(let1, runner);
		assertEquals(1, scn.GetKeywordCount(runner, Keyword.DAMAGE));
		assertInZone(Zone.DISCARD, twilight);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithoutInitiativeItIsNotPlayedFromTheDiscardPileThoughACopyInHandIsPlayable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var let1 = scn.GetShadowCard("let1");
		var let2 = scn.GetShadowCard("let2");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToDiscard(let1);
		scn.MoveCardsToHand(let2);
		scn.MoveCardsToHand(scn.GetFreepsCard("strike"), scn.GetFreepsCard("strike2"), scn.GetFreepsCard("lorien"),
				scn.GetFreepsCard("guard"));
		assertInZone(Zone.DISCARD, let1);
		assertInHand(let2);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Four cards in the Free Peoples hand: he has initiative.
		assertEquals(4, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowHasInitiative());
		assertInZone(Zone.DISCARD, let1);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(let1));
		// The initiative is asked of the play from the discard pile alone.
		assertTrue(scn.ShadowPlayAvailable(let2));
	}

	@Test
	public void WithInitiativeButNoGollumOrSmeagolItIsNotPlayedFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var let1 = scn.GetShadowCard("let1");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToDiscard(let1);
		scn.MoveCardsToHand(scn.GetFreepsCard("strike"), scn.GetFreepsCard("lorien"), scn.GetFreepsCard("guard"));
		assertInZone(Zone.DISCARD, let1);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// From the pile it is still the card it is: its "spot Gollum or Sméagol" is asked there too.
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertInZone(Zone.DISCARD, let1);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(let1));
	}

	@Test
	public void WithInitiativeItIsNotPlayedFromTheDiscardPileInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var let1 = scn.GetShadowCard("let1");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToDiscard(let1);
		scn.MoveCardsToHand(scn.GetFreepsCard("strike"), scn.GetFreepsCard("lorien"), scn.GetFreepsCard("guard"));
		assertInZone(Zone.DISCARD, let1);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, runner);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertInZone(Zone.DISCARD, let1);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(let1));
	}
}
