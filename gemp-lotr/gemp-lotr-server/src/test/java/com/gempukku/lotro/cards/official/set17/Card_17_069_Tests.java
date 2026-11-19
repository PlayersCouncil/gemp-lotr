package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("panic", "17_69");

					// Denizen of Khazad-dum: an [orc] Orc, strength 9, vitality 2; +2 at an underground site only
					put("denizen", "11_115");
					// Vicious Warg: an [orc] mount (+4, vitality +2) for an [orc] Orc of strength 9 or less; bearer is
					// fierce
					put("vicious", "17_92");
					// Orkish Cavalry: an [orc] Orc, strength 8, vitality 2; a draw when an [orc] mount is played, and an
					// Assignment ability that is not used
					put("cavalry", "17_74");
					// Threatening Warg: an [orc] mount (+2) for an [orc] Orc of strength 10 or less; bearer is fierce;
					// its play trigger is not reached (cheated on)
					put("threatening", "17_91");
					// Rider's Gear: [orc] armor (+2) for an [orc] Orc, a possession that is no mount; its wound bar
					// holds only while its bearer is mounted
					put("gear", "17_90");
					// Mordor Brute: a [sauron] Orc, strength 9; a [sauron] Orc bearing a possession takes no wounds
					// outside skirmishes
					put("brute", "10_90");
					// Gothmog's Warg: a [sauron] mount (+1) for a [sauron] Orc; strength +1 for each site its owner
					// controls (none here)
					put("gothmogswarg", "10_89");

					// Aragorn, Ranger of the North: strength 8, vitality 4; his Maneuver ability is not used
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: strength 6, vitality 3; his Skirmish ability is not used
					put("gimli", "1_13");

					// The Free Peoples hand: a companion, a possession, an event, and a Shadow card
					put("boromir", "1_97");
					put("armor", "1_92");
					put("swordarm", "1_116");
					// Goblin Patrol Troop: a [moria] minion with no text
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CryandPanicStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Cry and Panic
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a mounted [orc] Orc wins a skirmish, the Free Peoples player must reveal his or her hand and place two Free Peoples cards revealed on the bottom of his or her draw deck.
		*/

		var scn = GetScenario();

		var panic = scn.GetFreepsCard("panic");

		assertEquals("Cry and Panic", panic.getBlueprint().getTitle());
		assertNull(panic.getBlueprint().getSubtitle());
		assertFalse(panic.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, panic.getBlueprint().getSide());
		assertEquals(Culture.ORC, panic.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, panic.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(panic, Keyword.SUPPORT_AREA));
		assertEquals(2, panic.getBlueprint().getTwilightCost());
	}

	@Test
	public void AMountedOrcOrcWinningMakesTheFreePeoplesPlayerRevealHisHandAndPlaceTwoFreePeoplesCardsBeneathHisDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var swordarm = scn.GetFreepsCard("swordarm");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(aragorn);

		var panic = scn.GetShadowCard("panic");
		var denizen = scn.GetShadowCard("denizen");
		var vicious = scn.GetShadowCard("vicious");
		scn.MoveCardsToSupportArea(panic);
		assertInZone(Zone.SUPPORT, panic);

		scn.StartGame();
		scn.MoveCardsToHand(boromir, armor, swordarm, troop);
		assertInHand(boromir, armor, swordarm, troop);
		scn.MoveMinionsToTable(denizen);
		scn.AttachCardsTo(denizen, vicious);
		assertAttachedTo(vicious, denizen);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, denizen);
		scn.FreepsResolveSkirmish(aragorn);
		// 9 and the Warg's +4 against 8
		assertEquals(13, scn.GetStrength(denizen));
		assertEquals(4, scn.GetFreepsHandCount());
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// The hand is shown to the Shadow player, and its owner is shown nothing.
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		// Two Free Peoples cards, of three kinds; not the Shadow card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, armor, swordarm));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(boromir, swordarm);
		// He is asked which goes beneath first; the other, unasked, is the bottom card.
		scn.FreepsChooseCardBPFromSelection(boromir);

		assertInZone(Zone.DECK, boromir, swordarm);
		assertEquals(swordarm, scn.GetFreepsBottomOfDeck());
		assertEquals(boromir, scn.GetFromBottomOfFreepsDeck(2));
		assertInHand(armor, troop);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnOrcOrcThatIsNotMountedAndAMountedSauronOrcWinningSetNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var swordarm = scn.GetFreepsCard("swordarm");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var panic = scn.GetShadowCard("panic");
		var denizen = scn.GetShadowCard("denizen");
		var gear = scn.GetShadowCard("gear");
		var brute = scn.GetShadowCard("brute");
		var gothmogswarg = scn.GetShadowCard("gothmogswarg");
		scn.MoveCardsToSupportArea(panic);
		assertInZone(Zone.SUPPORT, panic);

		scn.StartGame();
		scn.MoveCardsToHand(boromir, armor, swordarm);
		assertInHand(boromir, armor, swordarm);
		scn.MoveMinionsToTable(denizen, brute);
		scn.AttachCardsTo(denizen, gear);
		scn.AttachCardsTo(brute, gothmogswarg);
		assertAttachedTo(gear, denizen);
		assertAttachedTo(gothmogswarg, brute);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, denizen}, new PhysicalCardImpl[]{gimli, brute});

		// An [orc] Orc bearing a possession that is no mount: 9 + 2 against 8.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(11, scn.GetStrength(denizen));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(3, scn.GetFreepsHandCount());

		// A mounted [sauron] Orc: 9 + 1 against 6.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(10, scn.GetStrength(brute));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertInHand(boromir, armor, swordarm);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMountedOrcOrcLosingSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var swordarm = scn.GetFreepsCard("swordarm");
		scn.MoveCompanionsToTable(aragorn);

		var panic = scn.GetShadowCard("panic");
		var denizen = scn.GetShadowCard("denizen");
		var vicious = scn.GetShadowCard("vicious");
		scn.MoveCardsToSupportArea(panic);
		assertInZone(Zone.SUPPORT, panic);

		scn.StartGame();
		scn.MoveCardsToHand(boromir, armor, swordarm);
		assertInHand(boromir, armor, swordarm);
		scn.MoveMinionsToTable(denizen);
		scn.AttachCardsTo(denizen, vicious);
		assertAttachedTo(vicious, denizen);
		// Aragorn made strong enough to beat the mounted Denizen, short of overwhelming him: 18 against 13.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, denizen);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(18, scn.GetStrength(aragorn));
		assertEquals(13, scn.GetStrength(denizen));
		scn.PassSkirmishActions();

		// Vitality 2 and the Warg's +2: he lives through the wound.
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		assertEquals(1, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertInHand(boromir, armor, swordarm);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void EachWinIsAnsweredTheFierceSkirmishToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var swordarm = scn.GetFreepsCard("swordarm");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(aragorn);

		var panic = scn.GetShadowCard("panic");
		var cavalry = scn.GetShadowCard("cavalry");
		var threatening = scn.GetShadowCard("threatening");
		scn.MoveCardsToSupportArea(panic);
		assertInZone(Zone.SUPPORT, panic);

		scn.StartGame();
		scn.MoveCardsToHand(boromir, armor, swordarm, gimli, troop);
		assertInHand(boromir, armor, swordarm, gimli, troop);
		scn.MoveMinionsToTable(cavalry);
		scn.AttachCardsTo(cavalry, threatening);
		assertAttachedTo(threatening, cavalry);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cavalry);
		scn.FreepsResolveSkirmish(aragorn);
		// A second title: 8 and the Warg's +2 against 8; the tie goes to the minion.
		assertEquals(10, scn.GetStrength(cavalry));
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		scn.ShadowDismissRevealedCards();
		scn.FreepsChooseCards(boromir, armor);
		scn.FreepsChooseCardBPFromSelection(boromir);
		assertInZone(Zone.DECK, boromir, armor);
		assertInHand(swordarm, gimli, troop);

		// The Warg makes him fierce: he wins again, and the trigger answers the second win.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, cavalry);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();
		// Two Free Peoples cards left: both go, with no choice of which; only the order is asked.
		scn.FreepsChooseCardBPFromSelection(swordarm);
		assertInZone(Zone.DECK, swordarm, gimli);
		assertEquals(gimli, scn.GetFreepsBottomOfDeck());
		assertInHand(troop);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
