package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("engineer", "5_61");
					put("engineer2", "5_61");
					// Battering Ram: an [isengard] machine, a condition; its Shadow and Skirmish abilities used only on purpose
					put("ram", "5_44");
					// Uruk Shield Wall: an [isengard] machine, a possession; its trigger answers only an Uruk-hai costing
					// as many as its tokens, outside a skirmish, about to be wounded
					put("wall", "102_35");
					// What Did You Discover?: an [isengard] support-area condition that is no machine; a regroup trigger only
					put("discover", "4_213");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, cost 2, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukEngineerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Uruk Engineer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>When you play this minion, you may place an [isengard] token on a machine.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("engineer");

		assertEquals("Uruk Engineer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedItMayPlaceAnIsengardTokenOnAMachineOfEitherType() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var engineer = scn.GetShadowCard("engineer");
		var ram = scn.GetShadowCard("ram");
		var wall = scn.GetShadowCard("wall");
		var discover = scn.GetShadowCard("discover");
		scn.MoveCardsToHand(engineer);
		scn.MoveCardsToSupportArea(ram, wall, discover);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(0, scn.GetCultureTokensOn(wall));
		assertEquals(0, scn.GetCultureTokensOn(discover));

		scn.ShadowPlayCard(engineer);
		assertInZone(Zone.SHADOW_CHARACTERS, engineer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(engineer));
		scn.ShadowAcceptOptionalTrigger();

		// A condition and a possession that are machines; an [isengard] support-area condition that is none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ram, wall));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(discover, engineer));
		scn.ShadowChooseCard(wall);

		assertEquals(1, scn.GetCultureTokensOn(wall));
		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(0, scn.GetCultureTokensOn(discover));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningPlacesNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var engineer = scn.GetShadowCard("engineer");
		var ram = scn.GetShadowCard("ram");
		var wall = scn.GetShadowCard("wall");
		scn.MoveCardsToHand(engineer);
		scn.MoveCardsToSupportArea(ram, wall);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(0, scn.GetCultureTokensOn(wall));

		scn.ShadowPlayCard(engineer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(engineer));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, engineer);
		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(0, scn.GetCultureTokensOn(wall));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMinionPlayedAfterItOffersNothingAndASecondCopyOffersOnlyItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var engineer = scn.GetShadowCard("engineer");
		var engineer2 = scn.GetShadowCard("engineer2");
		var savage = scn.GetShadowCard("savage");
		var ram = scn.GetShadowCard("ram");
		scn.MoveCardsToHand(engineer2, savage);
		scn.MoveCardsToSupportArea(ram);

		scn.StartGame();
		scn.MoveMinionsToTable(engineer);
		scn.SetTwilight(12);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, engineer);
		assertEquals(0, scn.GetCultureTokensOn(ram));

		// Another Uruk-hai played while this one is in play.
		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second copy played: its own offer, and none from the copy already in play.
		scn.ShadowPlayCard(engineer2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(engineer2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(engineer));
		scn.ShadowAcceptOptionalTrigger();

		// The one machine takes the token unasked, and nothing more is offered.
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoMachineInPlayNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var engineer = scn.GetShadowCard("engineer");
		var discover = scn.GetShadowCard("discover");
		scn.MoveCardsToHand(engineer);
		scn.MoveCardsToSupportArea(discover);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(discover));

		scn.ShadowPlayCard(engineer);
		assertInZone(Zone.SHADOW_CHARACTERS, engineer);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(discover));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
