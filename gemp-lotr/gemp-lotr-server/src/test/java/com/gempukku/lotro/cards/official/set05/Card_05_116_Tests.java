package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_05_116_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "5_116");
					// Sam, Son of Hamfast: a Ring-bound Hobbit companion who is not Frodo, strength 3, vitality 4;
					// his abilities are never used
					put("sam", "1_311");
					// Smeagol, Old Noser: [gollum] companion, strength 3, vitality 4; a regroup ability, never used
					put("smeagol", "5_28");
					// Smeagol, Once Like Us: the [shire] Smeagol, strength 3, vitality 4; an assignment ability,
					// never used
					put("shiresmeagol", "103_116");
					// Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4; a regroup ability,
					// never used
					put("gollum", "5_24");
					// Gollum, Half a Wraith Himself: the [wraith] Gollum, strength 5, vitality 4; his texts answer a
					// Nazgul played and the assignment phase, and never come up
					put("wraithgollum", "103_66");
					put("savage", "1_151");     // Uruk Savage: a minion that is no Gollum, strength 5, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: strength 3, vitality 4
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGaladrielRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "5_116");
					// Frodo, Frenzied Fighter: the one Frodo who cannot bear the Ring, so the one who may be in play
					// beside another Ring-bearer (Q-0159, Q-0212)
					put("frodo", "13_149");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit companion who is not Frodo
				}},
				VirtualTableScenario.FellowshipSites,
				// Galadriel, Bearer of Wisdom: her only text changes her own resistance
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Sting, Baggins Heirloom
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be Frodo.
		 * <b>Skirmish:</b> Exert Frodo to make Sméagol strength +2 or Gollum strength -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sting");

		assertEquals("Sting", card.getBlueprint().getTitle());
		assertEquals("Baggins Heirloom", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnFrodoTheRingBearerAndNotOnSam() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var sting = scn.GetFreepsCard("sting");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(sting);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(sting));

		// Frodo is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(sting);
		assertAttachedTo(sting, frodo);
		assertTrue(scn.GetAttachedCards(sam).isEmpty());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnlyOnceAFrodoIsInPlayAndThenOnHimThoughHeIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var sam = scn.GetFreepsCard("sam");
		var sting = scn.GetFreepsCard("sting");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(sting);

		scn.StartGame();

		// No Frodo: neither the Ring-bearer nor Sam may bear it.
		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sting));

		scn.MoveCompanionsToTable(frodo);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(sting));

		scn.FreepsPlayCard(sting);
		assertAttachedTo(sting, frodo);
		assertTrue(scn.GetAttachedCards(sam).isEmpty());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithBothInPlayFrodoExertsAndSmeagolIsStrengthPlusTwoForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gollum = scn.GetShadowCard("gollum");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(sam, smeagol);
		scn.AttachCardsTo(frodo, sting);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, savage}, new PhysicalCardImpl[]{smeagol, gollum});
		scn.FreepsResolveSkirmish(sam);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(5, scn.GetStrength(gollum));

		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		// Either can be done, so the choice is put.
		assertTrue(scn.FreepsChoiceAvailable("Sméagol"));
		assertTrue(scn.FreepsChoiceAvailable("Gollum"));
		scn.FreepsChoose("Sméagol");

		// 3 + 2, though Smeagol is in a skirmish still to come
		assertEquals(5, scn.GetStrength(smeagol));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own: the bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(smeagol);
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, gollum));
		assertEquals(3, scn.GetStrength(smeagol));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithBothInPlayTheGollumCultureGollumCanBeChosenInsteadAndIsStrengthMinusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gollum = scn.GetShadowCard("gollum");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(sam, smeagol);
		scn.AttachCardsTo(frodo, sting);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, savage}, new PhysicalCardImpl[]{smeagol, gollum});
		scn.FreepsResolveSkirmish(sam);

		assertEquals(Culture.GOLLUM, gollum.getBlueprint().getCulture());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(5, scn.GetStrength(gollum));

		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsChoiceAvailable("Sméagol"));
		assertTrue(scn.FreepsChoiceAvailable("Gollum"));
		scn.FreepsChoose("Gollum");

		// 5 - 2, though Gollum is in a skirmish still to come
		assertEquals(3, scn.GetStrength(gollum));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGollumTheShireSmeagolIsMadeStrengthPlusTwoWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(sam, smeagol);
		scn.AttachCardsTo(frodo, sting);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, savage);
		scn.FreepsResolveSkirmish(sam);

		assertEquals("Smeagol", smeagol.getBlueprint().getTitle());
		assertEquals(Culture.SHIRE, smeagol.getBlueprint().getCulture());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetStrength(smeagol));

		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		// 3 + 2
		assertEquals(5, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoSmeagolTheWraithGollumIsMadeStrengthMinusTwoWithNoQuestionForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var gollum = scn.GetShadowCard("wraithgollum");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, sting);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, savage}, new PhysicalCardImpl[]{frodo, gollum});
		scn.FreepsResolveSkirmish(sam);

		assertEquals(Culture.WRAITH, gollum.getBlueprint().getCulture());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(5, scn.GetStrength(gollum));

		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		// 5 - 2
		assertEquals(3, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Frodo's skirmish, the next phase: Gollum is 5 again.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, gollum));
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNeitherInPlayFrodoExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, sting);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, savage);
		scn.FreepsResolveSkirmish(sam);

		// No Smeagol and no Gollum: the exertion can be paid, so it is offered, and buys nothing.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(sting));
		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithFrodoExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(sam, smeagol);
		scn.AttachCardsTo(frodo, sting);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 3);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, gollum);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sting));
	}
}
