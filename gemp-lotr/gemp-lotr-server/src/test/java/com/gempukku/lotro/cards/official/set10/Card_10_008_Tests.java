package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cirdan", "10_8");
					put("lorien", "1_53");      // Lorien Elf: Elf companion; to play, spot an Elf, so cheated in
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, cost 2; a fellowship ability never used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man, strength 8; a maneuver ability never used here

					// [elven] events, one of each, only ever in a discard pile or a hand.  Defiance alone is
					// played: "Skirmish: Make an Elf strength +2 (or +4 if skirmishing a Nazgul)", cost 1.
					put("enmity", "1_29");      // Ancient Enmity
					put("border", "1_32");      // Border Defenses
					put("defiance", "1_37");    // Defiance
					put("doubleshot", "1_38");  // Double Shot: an Archery event
					put("elfsong", "1_39");     // Elf-song
					put("lightfoot", "1_52");   // Lightfootedness

					put("gilgalad", "1_66");    // The Tale of Gil-galad: [elven] condition an Elf bears; an [elven] card that is no event and no Elf; a skirmish ability used only on purpose
					put("axestrike", "1_3");    // Axe Strike: [dwarven] Skirmish event; an event that is not [elven]

					put("nelya", "1_233");      // Ulaire Nelya, Lieutenant of Morgul: [wraith] Nazgul, strength 10, vitality 3, fierce; a Shadow-phase ability never used here
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played, and it is cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CirdanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Círdan, The Shipwright
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: To play, spot 2 Elves.
		 * 	<b>Skirmish:</b> Exert Círdan to make a minion he is skirmishing strength -1 for each [elven] event in your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cirdan");

		assertEquals("Círdan", card.getBlueprint().getTitle());
		assertEquals("The Shipwright", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedWithOneElfAndIsPlayedOnceASecondIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var lorien = scn.GetFreepsCard("lorien");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		scn.MoveCompanionsToTable(lorien, aragorn);
		// The Elf bears an [elven] card that is no Elf; Aragorn is a companion who is no Elf.
		scn.AttachCardsTo(lorien, gilgalad);
		scn.MoveCardsToHand(cirdan, celeborn);

		scn.StartGame();
		// One Elf, a companion.
		assertAttachedTo(gilgalad, lorien);
		assertFalse(scn.FreepsPlayAvailable(cirdan));

		// A second Elf, an ally.
		scn.FreepsPlayCard(celeborn);
		assertInZone(Zone.SUPPORT, celeborn);
		assertTrue(scn.FreepsPlayAvailable(cirdan));

		scn.FreepsPlayCard(cirdan);
		assertInZone(Zone.FREE_CHARACTERS, cirdan);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsCirdanToMakeAMinionHeIsSkirmishingStrengthMinusOneForEachElvenEventInHisOwnersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(cirdan, aragorn);
		// Three [elven] events in the discard pile, beside an [elven] condition and a [dwarven] event.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("enmity"), scn.GetFreepsCard("border"), scn.GetFreepsCard("defiance"),
				scn.GetFreepsCard("gilgalad"), scn.GetFreepsCard("axestrike"));
		// An [elven] event in hand, and two in the other player's discard pile.
		scn.MoveCardsToHand(scn.GetFreepsCard("doubleshot"));
		scn.MoveCardsToDiscard(scn.GetShadowCard("elfsong"), scn.GetShadowCard("lightfoot"));

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{cirdan, savage}, new PhysicalCardImpl[]{aragorn, nelya});
		// The Goblin Runner left over is the Shadow player's to assign.
		scn.ShadowAssignToMinions(cirdan, runner);
		scn.FreepsResolveSkirmish(cirdan);

		assertEquals(0, scn.GetWoundsOn(cirdan));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(10, scn.GetStrength(nelya));
		assertTrue(scn.FreepsActionAvailable(cirdan));

		scn.FreepsUseCardAction(cirdan);
		assertEquals(1, scn.GetWoundsOn(cirdan));
		// The two minions Círdan is skirmishing, and not the one assigned to Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(nelya));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(savage);
		// 5 - 3
		assertEquals(2, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(10, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishCirdanExertsAndNoMinionIsWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(cirdan, aragorn);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("enmity"), scn.GetFreepsCard("border"), scn.GetFreepsCard("defiance"));

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{cirdan, savage}, new PhysicalCardImpl[]{aragorn, nelya});
		scn.FreepsResolveSkirmish(aragorn);

		// The action has a cost, so it is offered though Círdan is skirmishing nobody yet.
		assertEquals(0, scn.GetWoundsOn(cirdan));
		assertTrue(scn.FreepsActionAvailable(cirdan));

		scn.FreepsUseCardAction(cirdan);
		assertEquals(1, scn.GetWoundsOn(cirdan));
		// Neither the minion in the skirmish under way nor the minion Círdan is assigned to.
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheAssignmentPhaseNorOnceCirdanIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(cirdan);
		scn.AddWoundsToChar(cirdan, 2);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("enmity"));

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(cirdan));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(cirdan, savage);
		scn.FreepsResolveSkirmish(cirdan);
		// Vitality 4 with 2 wounds: he can exert once more.
		assertEquals(2, scn.GetWoundsOn(cirdan));
		assertTrue(scn.FreepsActionAvailable(cirdan));
		assertEquals(5, scn.GetStrength(savage));

		scn.FreepsUseCardAction(cirdan);
		assertEquals(3, scn.GetWoundsOn(cirdan));
		// 5 - 1
		assertEquals(4, scn.GetStrength(savage));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(cirdan));
	}

	@Test
	public void TheStrengthIsNotCountedAgainWhenAnotherElvenEventReachesTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var enmity = scn.GetFreepsCard("enmity");
		var border = scn.GetFreepsCard("border");
		var elfsong = scn.GetFreepsCard("elfsong");
		var defiance = scn.GetFreepsCard("defiance");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(cirdan);
		scn.MoveCardsToDiscard(enmity, border, elfsong);
		scn.MoveCardsToHand(defiance);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(cirdan, savage);
		scn.FreepsResolveSkirmish(cirdan);
		assertEquals(5, scn.GetStrength(savage));

		scn.FreepsUseCardAction(cirdan);
		// 5 - 3
		assertEquals(2, scn.GetStrength(savage));
		scn.ShadowPass();

		// A fourth [elven] event goes to the discard pile.  Círdan is the only Elf: 7 + 2.
		scn.FreepsPlayCard(defiance);
		assertInZone(Zone.DISCARD, defiance);
		assertEquals(9, scn.GetStrength(cirdan));
		// Still 5 - 3: what Círdan took off is not worked out again.
		assertEquals(2, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheMinionIsWeakenedOnlyForTheSkirmishInWhichCirdanExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable(cirdan);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("enmity"), scn.GetFreepsCard("border"),
				scn.GetFreepsCard("elfsong"), scn.GetFreepsCard("lightfoot"));

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(cirdan, nelya);
		scn.FreepsResolveSkirmish(cirdan);
		assertEquals(10, scn.GetStrength(nelya));

		scn.FreepsUseCardAction(cirdan);
		// 10 - 4
		assertEquals(6, scn.GetStrength(nelya));
		scn.ShadowPass();
		// At the last decision of the skirmish.
		assertEquals(6, scn.GetStrength(nelya));

		// 7 against 6: Ulaire Nelya loses and takes the skirmish's wound.
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);

		// Ulaire Nelya is fierce.  In her second skirmish, a phase of its own, her strength is the printed 10.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(cirdan, nelya);
		scn.FreepsResolveSkirmish(cirdan);
		assertEquals(10, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
