package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_281_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("towers1", "7_281");
					put("towers2", "7_281");
					put("engineer", "7_272");   // Gorgoroth Engineer: a besieger Orc; his text answers only his play from a stacked site
					// Great Hill Troll: a besieger who is a Troll (so a [sauron] minion who is no Orc); twilight cost -1 for
					// each [sauron] engine spotted; his trigger is for King-block sites only
					put("troll", "8_102");
					// Uruk Besieger: [isengard] Uruk-hai with "Besieger" in the title and not the keyword; damage +1, and a
					// regroup ability that stacks himself
					put("uruk", "4_180");
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc who is no besieger; his text answers a weapon played on him
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc; +4 and fierce only at 2 wounds on the Ring-bearer
					// Some Secret Art of Flame: another [sauron] condition that holds [sauron] tokens; a regroup ability,
					// and initiative at 3 tokens
					put("art", "7_313");
					put("grond", "8_103");      // Grond: a [sauron] possession that holds [sauron] tokens; abilities used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatSiegetowersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Great Siege-towers
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Engine</b>.<br><b>Shadow:</b> Play a besieger to place a [sauron] token here.<br><b>Skirmish:</b> Remove a [sauron] token from a condition to make a [sauron] Orc strength +1 (limit +2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("towers1");

		assertEquals("Great Siege-towers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.ENGINE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityPlaysABesiegerFromHandToPlaceATokenOnThisCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var towers1 = scn.GetShadowCard("towers1");
		var towers2 = scn.GetShadowCard("towers2");
		var engineer = scn.GetShadowCard("engineer");
		var troll = scn.GetShadowCard("troll");
		var uruk = scn.GetShadowCard("uruk");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.MoveCardsToSupportArea(towers1, towers2);

		scn.StartGame();
		scn.SetShadowHand(engineer, troll, uruk, chieftain);
		// Enough for any of the four.
		scn.SetTwilight(20);
		assertEquals(0, scn.GetCultureTokensOn(towers1));
		assertEquals(0, scn.GetCultureTokensOn(towers2));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(towers1));
		scn.ShadowUseCardAction(towers1);

		// The besieger Orc and the besieger Troll; not the Uruk-hai with the word in his title only, nor the [sauron]
		// Orc who is no besieger.
		assertTrue(scn.ShadowHasCardChoiceAvailable(engineer, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(uruk, chieftain));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(engineer);

		assertInZone(Zone.SHADOW_CHARACTERS, engineer);
		assertInHand(troll, uruk, chieftain);
		// The token is on the copy used, not the other.
		assertEquals(1, scn.GetCultureTokensOn(towers1));
		assertEquals(0, scn.GetCultureTokensOn(towers2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilTheOneBesiegerInHandCanBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var towers1 = scn.GetShadowCard("towers1");
		var engineer = scn.GetShadowCard("engineer");
		var uruk = scn.GetShadowCard("uruk");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.MoveCardsToSupportArea(towers1);

		scn.StartGame();
		scn.SetShadowHand(engineer, uruk, chieftain);
		scn.FreepsPass();

		// The Engineer costs 3 + 2 roaming at site 2 = 5; the Uruk Besieger and the Chieftain 2 + 2 roaming = 4 each.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(uruk));
		assertTrue(scn.ShadowPlayAvailable(chieftain));
		assertFalse(scn.ShadowPlayAvailable(engineer));
		assertFalse(scn.ShadowActionAvailable(towers1));

		// One more and the Engineer can be played, and the ability is offered.
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(engineer));
		assertTrue(scn.ShadowActionAvailable(towers1));
	}

	@Test
	public void SkirmishAbilityRemovesATokenFromAConditionToMakeASauronOrcStrengthPlus1UpToPlus2EachSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var towers1 = scn.GetShadowCard("towers1");
		var art = scn.GetShadowCard("art");
		var grond = scn.GetShadowCard("grond");
		var engineer = scn.GetShadowCard("engineer");
		var chieftain = scn.GetShadowCard("chieftain");
		var troll = scn.GetShadowCard("troll");
		var hound = scn.GetShadowCard("hound");

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(towers1, art, grond);
		scn.AddTokensToCard(towers1, 2);
		scn.AddTokensToCard(art, 2);
		scn.AddTokensToCard(grond, 1);
		assertEquals(2, scn.GetCultureTokensOn(towers1));
		assertEquals(2, scn.GetCultureTokensOn(art));
		assertEquals(1, scn.GetCultureTokensOn(grond));

		scn.StartGame();
		scn.MoveMinionsToTable(engineer, chieftain, troll, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, engineer}, new PhysicalCardImpl[]{gimli, chieftain});
		// The Troll and the Hound are left out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(engineer));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.ShadowActionAvailable(towers1));
		scn.ShadowUseCardAction(towers1);

		// A token from this card or another condition; not from the possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(towers1, art));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(grond));
		scn.ShadowChooseCard(art);
		assertEquals(1, scn.GetCultureTokensOn(art));
		assertEquals(2, scn.GetCultureTokensOn(towers1));

		// The [sauron] Orc in this skirmish and the one in another; not the [sauron] Troll or the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(engineer, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(engineer);
		assertEquals(10, scn.GetStrength(engineer));

		// The second +1, to another Orc.
		scn.FreepsPass();
		scn.ShadowUseCardAction(towers1);
		scn.ShadowChooseCard(towers1);
		scn.ShadowChooseCard(chieftain);
		assertEquals(1, scn.GetCultureTokensOn(towers1));
		assertEquals(8, scn.GetStrength(chieftain));
		assertEquals(10, scn.GetStrength(engineer));

		// At the limit of +2 the ability is still offered, as it has a cost: the token goes and nothing is gained.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(towers1));
		scn.ShadowUseCardAction(towers1);
		scn.ShadowChooseCard(towers1);
		scn.ShadowChooseCard(engineer);
		assertEquals(0, scn.GetCultureTokensOn(towers1));
		assertEquals(10, scn.GetStrength(engineer));
		assertEquals(8, scn.GetStrength(chieftain));
		assertEquals(1, scn.GetCultureTokensOn(grond));

		// In the next skirmish both bonuses are gone, and the limit starts again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(9, scn.GetStrength(engineer));
		assertEquals(7, scn.GetStrength(chieftain));
		scn.FreepsPass();
		scn.ShadowUseCardAction(towers1);
		// The Art's last token: the only one left on a condition, taken unasked.
		scn.ShadowChooseCard(chieftain);
		assertEquals(0, scn.GetCultureTokensOn(art));
		assertEquals(8, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithATokenOnlyOnAPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var towers1 = scn.GetShadowCard("towers1");
		var grond = scn.GetShadowCard("grond");
		var engineer = scn.GetShadowCard("engineer");

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(towers1, grond);
		scn.AddTokensToCard(grond, 1);
		assertEquals(0, scn.GetCultureTokensOn(towers1));
		assertEquals(1, scn.GetCultureTokensOn(grond));

		scn.StartGame();
		scn.MoveMinionsToTable(engineer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, engineer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(towers1));
		assertEquals(9, scn.GetStrength(engineer));
	}
}
