package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_158_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("raiding", "51_158");
					put("raiding2", "51_158");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, strength 5; Damage +1 is its whole text
					put("savage2", "1_151");
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai minion, strength 8; Damage +1 is its whole text
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc minion, strength 7; a regroup ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: Free Peoples companion, strength 3; made an Uruk-hai in one test
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukhaiRaidingPartyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk-hai Raiding Party
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * While you can spot 2 Uruk-hai, each Uruk-hai is strength +1.
		 * While you can spot 4 Uruk-hai, this minion is <b>fierce</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("raiding");

		assertEquals("Uruk-hai Raiding Party", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForFourPlusRoamingItMakesItselfAndTheUrukHaiAlreadyThereStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var raiding = scn.GetShadowCard("raiding");
		var savage = scn.GetShadowCard("savage");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(raiding);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, servant);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(servant));
		assertTrue(scn.ShadowPlayAvailable(raiding));
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(raiding);
		// 4 to play + 2 roaming (a site 5 minion at site 2)
		assertEquals(twilight - 6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, raiding);
		// Two Uruk-hai: each is +1.  The [isengard] Orc is not an Uruk-hai.
		assertEquals(10, scn.GetStrength(raiding));
		assertEquals(6, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(servant));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcDoesNotMakeASecondUrukHaiAndAnUrukHaiOfAnotherCultureDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var raiding = scn.GetShadowCard("raiding");
		var sentry = scn.GetShadowCard("sentry");
		var servant = scn.GetShadowCard("servant");

		scn.StartGame();
		scn.MoveMinionsToTable(raiding, servant);
		scn.FreepsPass();

		// One Uruk-hai and an [isengard] Orc: no bonus.
		assertEquals(9, scn.GetStrength(raiding));
		assertEquals(7, scn.GetStrength(servant));

		scn.MoveMinionsToTable(sentry);
		// The [sauron] Uruk-hai makes two, and is +1 itself.
		assertEquals(10, scn.GetStrength(raiding));
		assertEquals(9, scn.GetStrength(sentry));
		assertEquals(7, scn.GetStrength(servant));

		scn.MoveCardsToDiscard(sentry);
		// Back to one Uruk-hai: the bonus is gone.
		assertEquals(9, scn.GetStrength(raiding));
		assertEquals(7, scn.GetStrength(servant));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoRaidingPartiesMakeEachUrukHaiStrengthPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var raiding = scn.GetShadowCard("raiding");
		var raiding2 = scn.GetShadowCard("raiding2");
		var savage = scn.GetShadowCard("savage");
		var servant = scn.GetShadowCard("servant");

		scn.StartGame();
		scn.MoveMinionsToTable(raiding, savage, servant);
		scn.FreepsPass();

		assertEquals(10, scn.GetStrength(raiding));
		assertEquals(6, scn.GetStrength(savage));

		scn.MoveMinionsToTable(raiding2);
		// Each copy's text makes each Uruk-hai +1: 9 + 1 + 1, and 5 + 1 + 1.
		assertEquals(11, scn.GetStrength(raiding));
		assertEquals(11, scn.GetStrength(raiding2));
		assertEquals(7, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(servant));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUrukHaiOfTheFreePeoplesPlayerIsCountedAndStrengthenedToo() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetScenario();

		var raiding = scn.GetShadowCard("raiding");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		var sam = scn.GetFreepsCard("sam");
		// Sam is made an Uruk-hai wherever he is: the text names no side, so he counts and is strengthened like
		// the Shadow player's own.  The modifier goes by his name because he is on the table before the game starts.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, scn.GenerateFreepsFilter("name(Sam)"), null, Race.URUK_HAI));
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(raiding);
		scn.FreepsPass();

		// The Raiding Party is the only Shadow Uruk-hai: Sam makes two.
		assertEquals(10, scn.GetStrength(raiding));
		assertEquals(4, scn.GetStrength(sam));
		assertFalse(scn.HasKeyword(raiding, Keyword.FIERCE));

		scn.MoveMinionsToTable(savage);
		// Three Uruk-hai, Sam one of them.
		assertEquals(6, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(raiding, Keyword.FIERCE));

		scn.MoveMinionsToTable(sentry);
		// Four Uruk-hai, but only three of them the Shadow player's.
		assertTrue(scn.HasKeyword(raiding, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sam, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FierceOnlyWhileFourUrukHaiCanBeSpottedAndNoOtherUrukHaiIsMadeFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var raiding = scn.GetShadowCard("raiding");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var sentry = scn.GetShadowCard("sentry");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(savage2);

		scn.StartGame();
		scn.MoveMinionsToTable(raiding, savage, sentry, servant);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Three Uruk-hai and an [isengard] Orc: four minions, three Uruk-hai.
		assertFalse(scn.HasKeyword(raiding, Keyword.FIERCE));

		scn.ShadowPlayCard(savage2);
		assertInZone(Zone.SHADOW_CHARACTERS, savage2);
		// Four Uruk-hai, the [sauron] one among them.
		assertTrue(scn.HasKeyword(raiding, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(servant, Keyword.FIERCE));

		scn.MoveCardsToDiscard(sentry);
		assertFalse(scn.HasKeyword(raiding, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
