package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sofair", "8_111");

					// Unbound Hobbits of two cultures
					put("merry", "1_303");       // Merry, From O'er the Brandywine: [shire]; +2 only while he bears a weapon
					put("pippin", "103_31");     // Pippin, Trollslayer: a [gondor] Hobbit, enduring; a skirmish ability used only on purpose
					// and those who are not
					put("sam", "1_311");         // Sam, Son of Hamfast: a Ring-bound [shire] Hobbit; abilities used only on purpose
					put("bounder", "1_286");     // Bounder: a [shire] Hobbit ally; a skirmish ability
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: unbound, no Hobbit, [gondor]; a maneuver ability

					// [shire] cards that are no character
					put("sword", "1_299");       // Hobbit Sword: [shire] possession with no other text
					put("sword2", "1_299");      // a second, for the hand
					put("fearing", "2_100");     // Fearing the Worst: [shire] support condition; it raises search cards and trackers only

					put("savage", "1_151");      // Uruk Savage: strength 5, damage +1
					put("runner", "1_178");      // Goblin Runner: strength 5, vitality 1; its text is about being played
					// The Free Peoples player's own copies of these are cards to hold in hand: he can play none of them.
					put("troop", "1_177");       // Goblin Patrol Troop: no text
					put("marksman", "1_176");    // Goblin Marksman: archer and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetInitiativeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sofair", "8_111");
					// Pippin, Friend to Frodo: unbound Hobbit, strength 3.  His text is about [shire] tales.
					put("pippin", "1_306");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1
					// The Free Peoples player's own copies of these are cards to hold in hand: he can play none
					// of them.
					put("troop", "1_177");      // Goblin Patrol Troop: no text
					put("runner", "1_178");     // Goblin Runner: its text is about being played
					put("marksman", "1_176");   // Goblin Marksman: archer and nothing else
					put("brigand", "4_10");     // Dunlending Brigand: no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SoFairSoDesperateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: So Fair, So Desperate
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: If the Shadow has initiative, add a threat to make an unbound Hobbit strength +1 for each [shire] card you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sofair");

		assertEquals("So Fair, So Desperate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Comprehensive Rules 4.2, "playing a card": "You cannot count The Card for initiative."  For a Free Peoples
	// event that needs the SHADOW to have initiative that is three cards or fewer beside it (F-0464; Ketura,
	// 2026-10-11).
	@Test
	public void WithFourCardsInHandBesideItItCannotBePlayedAndWithFourItselfAmongThemItCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetInitiativeScenario();

		var sofair = scn.GetFreepsCard("sofair");
		var pippin = scn.GetFreepsCard("pippin");
		var card1 = scn.GetFreepsCard("troop");
		var card2 = scn.GetFreepsCard("runner");
		var card3 = scn.GetFreepsCard("marksman");
		var card4 = scn.GetFreepsCard("brigand");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(sofair, card1, card2, card3, card4);
		assertInHand(sofair, card1, card2, card3, card4);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(pippin, savage);

		// Five cards in hand, four beside the event: the Shadow does not have initiative, and the event cannot
		// be played.
		assertEquals(5, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, sofair);
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sofair));

		// Four cards, the event one of them.  The Free Peoples player has initiative while it lies in his hand;
		// not counting it he has three cards, the Shadow has initiative, and the event can be played.
		scn.MoveCardsToDiscard(card4);
		scn.FreepsRefreshDecision();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsPlayAvailable(sofair));
		assertEquals(0, scn.GetThreats());
		assertEquals(3, scn.GetStrength(pippin));

		scn.FreepsPlayCard(sofair);
		// Frodo and Pippin are the [shire] cards to spot.
		scn.FreepsChoose("2");

		assertEquals(1, scn.GetThreats());
		// 3 + 1 for each of Frodo and Pippin
		assertEquals(5, scn.GetStrength(pippin));
		assertInZone(Zone.DISCARD, sofair);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnUnboundHobbitOfEitherCultureIsStrengthPlus1ForEachShireCardSpottedUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sofair = scn.GetFreepsCard("sofair");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var sword2 = scn.GetFreepsCard("sword2");
		var fearing = scn.GetFreepsCard("fearing");
		var troop = scn.GetFreepsCard("troop");
		var marksman = scn.GetFreepsCard("marksman");
		scn.MoveCompanionsToTable(merry, pippin, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(sam, sword);
		scn.MoveCardsToSupportArea(fearing);
		scn.MoveCardsToHand(sofair, sword2, troop, marksman);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		assertAttachedTo(sword, sam);
		assertInZone(Zone.SUPPORT, fearing);
		assertInHand(sofair, sword2, troop, marksman);
		scn.SkipToAssignments();
		// Merry's skirmish first; Pippin's comes after it.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, savage},
				new PhysicalCardImpl[]{pippin, runner});
		scn.FreepsResolveSkirmish(merry);

		// Four cards in hand, the event among them: the Shadow has initiative.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.FreepsPlayAvailable(sofair));
		scn.FreepsPlayCard(sofair);
		assertEquals(1, scn.GetThreats());

		// Frodo, Merry, Sam, Bounder, the Hobbit Sword Sam bears and Fearing the Worst are the [shire] cards in
		// play.  Not Pippin or Aragorn ([gondor]), the Hobbit Sword in hand, or the event itself.  The player may
		// spot fewer; the number comes filled in with all of them.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(6, scn.FreepsGetChoiceMax());
		assertEquals(6, (int) scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("4");

		// An unbound Hobbit, in this skirmish or in one still to come; not a Ring-bound Hobbit, a Hobbit ally, or an
		// unbound companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, bounder, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.GetStrength(pippin));
		scn.FreepsChooseCard(pippin);

		// 3 + 4
		assertEquals(7, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(merry));
		assertInDiscard(sofair);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The bonus lasts the skirmish it was played in; in Pippin's own skirmish it is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileThereAreAsManyThreatsAsCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sofair = scn.GetFreepsCard("sofair");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword2 = scn.GetFreepsCard("sword2");
		var troop = scn.GetFreepsCard("troop");
		var marksman = scn.GetFreepsCard("marksman");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(sofair, sword2, troop, marksman);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		assertInHand(sofair, sword2, troop, marksman);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, savage);

		// Frodo and Merry, and two threats: none more can be added.
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sofair));

		// A third companion, and a third threat can be added.
		scn.MoveCompanionsToTable(aragorn);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(sofair));
	}

	@Test
	public void WithNoUnboundHobbitTheEventIsPlayedAndTheThreatAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sofair = scn.GetFreepsCard("sofair");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword2 = scn.GetFreepsCard("sword2");
		var troop = scn.GetFreepsCard("troop");
		var marksman = scn.GetFreepsCard("marksman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sofair, sword2, troop, marksman);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		assertInHand(sofair, sword2, troop, marksman);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, savage);

		// Frodo is a Ring-bound Hobbit and Aragorn an unbound companion who is no Hobbit: no unbound Hobbit.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetThreats());
		var strength = scn.GetStrength(frodo);
		assertTrue(scn.FreepsPlayAvailable(sofair));

		scn.FreepsPlayCard(sofair);
		assertEquals(1, scn.GetThreats());

		// Frodo is the one [shire] card to spot; Aragorn is [gondor].  No Hobbit is then asked for.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		assertInDiscard(sofair);
		assertEquals(strength, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
