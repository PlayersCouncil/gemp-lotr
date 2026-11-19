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

public class Card_05_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ram", "5_44");
					put("ram2", "5_44");        // a second copy: the card is not unique and its token is "here"

					put("troop", "1_143");      // Troop of Uruk-hai: [isengard] Uruk-hai, cost 5, site 5, vitality 4; damage +1, weapons give no bonus against it
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("legion", "17_129");    // White Hand Legion: [uruk-hai] Uruk-hai, strength 17, vitality 3; damage +1, to play remove 2 threats (cheated in)
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, vitality 2; "Shadow: Exert this minion to add (1)", used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("knight", "5_35");      // Gondorian Knight: strength 6, no text but knight
					put("kog", "5_36");         // Knight of Gondor: a fellowship ability, never used
					put("ranger", "7_116");     // Ranger of Osgiliath: a regroup ability, never used
					// Thrarin, Dwarven Smith: an ally, who nearly counts as a companion; his maneuver ability is never used
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BatteringRamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Battering Ram
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: <b>Machine</b>. Plays to your support area.
		 * <b>Shadow:</b> Exert an Uruk-hai to place an [isengard] token on this card.
		 * <b>Skirmish:</b> Spot 5 companions and an [isengard] token here to make an Uruk-hai <b>damage</b> <b>+1</b>.
		 * Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ram");

		assertEquals("Battering Ram", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MACHINE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnUrukHaiToPlaceATokenUntilNoUrukHaiCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ram = scn.GetShadowCard("ram");
		var troop = scn.GetShadowCard("troop");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToSupportArea(ram);
		scn.MoveCardsToHand(troop);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, ram);
		assertInHand(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(forger);
		scn.ShadowRefreshDecision();

		// No Uruk-hai to exert, only an [isengard] Orc.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ram));
		scn.ShadowPlayCard(troop);

		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowActionAvailable(ram));

		scn.ShadowUseCardAction(ram);
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertEquals(1, scn.GetWoundsOn(troop));
		// Placing a token is not what discards the Ram.
		assertInZone(Zone.SUPPORT, ram);
		assertTrue(scn.ShadowActionAvailable(ram));

		scn.ShadowUseCardAction(ram);
		assertEquals(2, scn.GetCultureTokensOn(ram));
		assertEquals(2, scn.GetWoundsOn(troop));

		// No limit is printed: a third token in the same phase.
		assertTrue(scn.ShadowActionAvailable(ram));
		scn.ShadowUseCardAction(ram);
		assertEquals(3, scn.GetCultureTokensOn(ram));
		assertEquals(3, scn.GetWoundsOn(troop));

		// Vitality 4 with 3 wounds: the Uruk-hai cannot pay again, and the Orc, who could exert, may not.
		assertEquals(0, scn.GetWoundsOn(forger));
		assertFalse(scn.IsExhausted(forger));
		assertFalse(scn.ShadowActionAvailable(ram));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUrukHaiOfAnyCultureMayExertAndTheTokenGoesOnTheCopyUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ram = scn.GetShadowCard("ram");
		var ram2 = scn.GetShadowCard("ram2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var legion = scn.GetShadowCard("legion");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToSupportArea(ram, ram2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, ram, ram2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop, savage, legion, forger);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(troop, 3);
		assertEquals(3, scn.GetWoundsOn(troop));
		scn.ShadowRefreshDecision();

		scn.ShadowUseCardAction(ram);
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, legion));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(legion);
		assertEquals(1, scn.GetWoundsOn(legion));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(forger));
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertEquals(0, scn.GetCultureTokensOn(ram2));

		scn.ShadowUseCardAction(ram2);
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertEquals(1, scn.GetCultureTokensOn(ram2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFiveCompanionsAndATokenHereMakesAnUrukHaiDamagePlusOneForThatSkirmishAndDiscardsTheCopyUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ram = scn.GetShadowCard("ram");
		var ram2 = scn.GetShadowCard("ram2");
		var savage = scn.GetShadowCard("savage");
		var legion = scn.GetShadowCard("legion");
		var forger = scn.GetShadowCard("forger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		// Five companions, with Frodo.
		scn.MoveCompanionsToTable(aragorn, knight, kog, ranger);
		scn.MoveCardsToSupportArea(ram, ram2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, legion, forger);
		// The Ram places tokens on itself; the second copy has none.
		scn.AddTokensToCard(ram, 1);
		assertInZone(Zone.SUPPORT, ram, ram2);
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertEquals(0, scn.GetCultureTokensOn(ram2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{knight, legion});
		// The Orc is left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The copy with a token is offered; the copy beside it, with none of its own, is not.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ram));
		assertFalse(scn.ShadowActionAvailable(ram2));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(legion, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(forger, Keyword.DAMAGE));

		scn.ShadowUseCardAction(ram);
		// An Uruk-hai of either culture, in the skirmish or out of it; not the [isengard] Orc, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, legion));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(legion);

		// Damage +1 on the Legion's own damage +1.
		assertEquals(2, scn.GetKeywordCount(legion, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(forger, Keyword.DAMAGE));
		// The copy used is discarded; the other stays.
		assertInZone(Zone.DISCARD, ram);
		assertInZone(Zone.SUPPORT, ram2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Legion's own skirmish is a phase of its own: the damage +1 is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(knight);
		assertEquals(1, scn.GetKeywordCount(legion, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithFourCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ram = scn.GetShadowCard("ram");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var thrarin = scn.GetFreepsCard("thrarin");
		// Four companions, with Frodo, and an ally.
		scn.MoveCompanionsToTable(aragorn, knight, kog);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(ram);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddTokensToCard(ram, 1);
		assertInZone(Zone.SUPPORT, ram);
		assertEquals(1, scn.GetCultureTokensOn(ram));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ram));
		assertInZone(Zone.SUPPORT, ram);
	}

	@Test
	public void WithNoUrukHaiItIsOfferedAndDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ram = scn.GetShadowCard("ram");
		var forger = scn.GetShadowCard("forger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(aragorn, knight, kog, ranger);
		scn.MoveCardsToSupportArea(ram);

		scn.StartGame();
		scn.MoveMinionsToTable(forger);
		scn.AddTokensToCard(ram, 1);
		assertInZone(Zone.SUPPORT, ram);
		assertEquals(1, scn.GetCultureTokensOn(ram));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, forger);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The only minion is an Orc. The ability is offered: its second sentence still happens.
		assertTrue(scn.ShadowActionAvailable(ram));
		scn.ShadowUseCardAction(ram);

		assertEquals(0, scn.GetKeywordCount(forger, Keyword.DAMAGE));
		assertInZone(Zone.DISCARD, ram);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
