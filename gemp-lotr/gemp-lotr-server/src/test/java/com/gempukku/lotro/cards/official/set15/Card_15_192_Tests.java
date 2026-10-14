package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_192_Tests
{

	// Isengard Ruined stands in each adventure deck in place of site 9, which no test reaches, and is put on the
	// path as site 2.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability only
					put("knight", "5_35");          // Gondorian Knight: [gondor] Man companion, vitality 3; Knight is his whole text
					put("grimir", "1_17");          // Grimir, Dwarven Elder: an ally of vitality 3; a fellowship ability only
					// Elendil's Army: [gondor] follower, strength +2; Aid: exert a [gondor] companion
					put("army", "18_47");
					// Saruman, Servant of Sauron: [isengard] Shadow follower, Aid (1) removed, to a minion; its other text
					// needs its bearer to win a skirmish
					put("saruman", "17_38");
					// First Level: [gondor] fortification condition; Maneuver: exert a [gondor] Man (or spot a knight) to
					// transfer it from the support area to a minion
					put("firstlevel", "7_92");

					put("raiders", "1_158");        // Uruk-hai Raiding Party: vitality 3; Damage +1 is its whole text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("ruined", "15_192");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The turn-skippers walk a Fellowship-block path.  Isengard Ruined stands in each adventure deck in place of
	// site 9, which the test does not reach, and is put on the path as site 2.  Two followers and their bearers.
	protected VirtualTableScenario GetFollowerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("radagast", "13_38");   // Radagast, Tender of Beasts: follower, Aid (2); its text needs 2 [gandalf] companions
					// Pallando, Far-travelling One: follower, Aid (2); exerts a bearer who is not a [gandalf] companion
					put("pallando", "13_37");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4
					put("savage", "1_151");     // Uruk Savage: so that there is a maneuver phase
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("ruined", "15_192");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardRuinedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Isengard Ruined
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>Battleground</b>. Each time a follower is transferred to a character, the first Shadow player may exert a companion twice.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("ruined");

		assertEquals("Isengard Ruined", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	// "Each time a follower is transferred to a character, the first Shadow player may exert a companion
	// twice": a "may" on a trigger is asked and may be declined (Q-0021; Q-0220: G).  The definition had the
	// Shadow player choose a companion each time, with no way to choose none.
	// F-0461.
	@Test
	public void TheShadowPlayerIsAskedEachTimeAndMayDecline() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFollowerScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var pallando = scn.GetFreepsCard("pallando");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(radagast, pallando);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("ruined"));
		scn.FreepsPass();
		var ruined = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// Radagast's Aid is paid and he goes to Aragorn: the Shadow player is asked, and declines.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(radagast));
		scn.FreepsChooseOptionalTrigger(radagast);
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(radagast, aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruined));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Pallando's Aid is paid and he goes to Gandalf: asked again, and this time Aragorn exerts twice.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(gandalf);
		assertAttachedTo(pallando, gandalf);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruined));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayExertACompanionWhoCanExertTwiceEachTimeAFollowerOfEitherSideIsTransferred() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var grimir = scn.GetFreepsCard("grimir");
		var army = scn.GetFreepsCard("army");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(army);
		// Vitality 3 with 1 wound: the Knight can exert once more, not twice.
		scn.AddWoundsToChar(knight, 1);

		var saruman = scn.GetShadowCard("saruman");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(saruman);

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("ruined"));
		scn.FreepsPass();
		var ruined = scn.GetCurrentSite();
		assertEquals("Isengard Ruined", ruined.getBlueprint().getTitle());
		scn.MoveMinionsToTable(raiders);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(raiders));

		// The Free Peoples follower: Aragorn exerts for the Aid, and the Army goes to Frodo.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(army));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(army, frodo);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruined));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ruined));
		scn.ShadowAcceptOptionalTrigger();
		// Companions who can exert twice are offered; the Knight, who can exert once, an ally and a minion are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(knight, grimir, raiders));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);
		// 1 + 2
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(raiders));

		// The Shadow follower, transferred to a minion: asked again, and declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowAcceptOptionalTrigger();
		assertAttachedTo(saruman, raiders);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruined));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AConditionTransferredToACharacterIsNoFollowerAndOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var firstlevel = scn.GetFreepsCard("firstlevel");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(firstlevel);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("ruined"));
		scn.FreepsPass();
		var ruined = scn.GetCurrentSite();
		assertEquals("Isengard Ruined", ruined.getBlueprint().getTitle());
		scn.MoveMinionsToTable(raiders);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsUseCardAction(firstlevel);
		assertAttachedTo(firstlevel, raiders);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ruined));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
	}

	@Test
	public void WithNoCompanionAbleToExertNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var saruman = scn.GetShadowCard("saruman");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(saruman);

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("ruined"));
		scn.FreepsPass();
		var ruined = scn.GetCurrentSite();
		assertEquals("Isengard Ruined", ruined.getBlueprint().getTitle());
		scn.MoveMinionsToTable(raiders);
		// Frodo, the only companion, is exhausted: vitality 4 with 3 wounds.
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowAcceptOptionalTrigger();
		assertAttachedTo(saruman, raiders);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ruined));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(3, scn.GetWoundsOn(frodo));
	}
}
