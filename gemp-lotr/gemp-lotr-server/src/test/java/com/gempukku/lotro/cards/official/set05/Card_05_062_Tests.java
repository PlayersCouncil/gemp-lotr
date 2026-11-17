package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_062_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sapper", "5_62");
					put("sapper2", "5_62");
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
	public void UrukSapperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Uruk Sapper
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>When you play this minion, you may place an [isengard] token on a machine.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sapper");

		assertEquals("Uruk Sapper", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedItMayPlaceAnIsengardTokenOnAMachineOfEitherType() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sapper = scn.GetShadowCard("sapper");
		var ram = scn.GetShadowCard("ram");
		var wall = scn.GetShadowCard("wall");
		var discover = scn.GetShadowCard("discover");
		scn.MoveCardsToHand(sapper);
		scn.MoveCardsToSupportArea(ram, wall, discover);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(0, scn.GetCultureTokensOn(wall));
		assertEquals(0, scn.GetCultureTokensOn(discover));

		scn.ShadowPlayCard(sapper);
		assertInZone(Zone.SHADOW_CHARACTERS, sapper);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sapper));
		scn.ShadowAcceptOptionalTrigger();

		// A condition and a possession that are machines; an [isengard] support-area condition that is none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ram, wall));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(discover, sapper));
		scn.ShadowChooseCard(wall);

		assertEquals(1, scn.GetCultureTokensOn(wall));
		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(0, scn.GetCultureTokensOn(discover));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningPlacesNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sapper = scn.GetShadowCard("sapper");
		var ram = scn.GetShadowCard("ram");
		var wall = scn.GetShadowCard("wall");
		scn.MoveCardsToHand(sapper);
		scn.MoveCardsToSupportArea(ram, wall);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(0, scn.GetCultureTokensOn(wall));

		scn.ShadowPlayCard(sapper);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sapper));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, sapper);
		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(0, scn.GetCultureTokensOn(wall));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMinionPlayedAfterItOffersNothingAndASecondCopyOffersOnlyItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sapper = scn.GetShadowCard("sapper");
		var sapper2 = scn.GetShadowCard("sapper2");
		var savage = scn.GetShadowCard("savage");
		var ram = scn.GetShadowCard("ram");
		scn.MoveCardsToHand(sapper2, savage);
		scn.MoveCardsToSupportArea(ram);

		scn.StartGame();
		scn.MoveMinionsToTable(sapper);
		scn.SetTwilight(12);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, sapper);
		assertEquals(0, scn.GetCultureTokensOn(ram));

		// Another Uruk-hai played while this one is in play.
		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second copy played: its own offer, and none from the copy already in play.
		scn.ShadowPlayCard(sapper2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sapper2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sapper));
		scn.ShadowAcceptOptionalTrigger();

		// The one machine takes the token unasked, and nothing more is offered.
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoMachineInPlayNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sapper = scn.GetShadowCard("sapper");
		var discover = scn.GetShadowCard("discover");
		scn.MoveCardsToHand(sapper);
		scn.MoveCardsToSupportArea(discover);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(discover));

		scn.ShadowPlayCard(sapper);
		assertInZone(Zone.SHADOW_CHARACTERS, sapper);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(discover));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
