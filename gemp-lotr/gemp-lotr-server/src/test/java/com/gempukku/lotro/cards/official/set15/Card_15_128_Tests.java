package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_128_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haleth", "15_128");
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion; a skirmish ability used only on purpose
					put("rider2", "4_286");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, knight, no other text
					put("mount1", "4_287");     // Rider's Mount: [rohan] mount, no strength; its text answers a skirmish only
					put("mount2", "4_287");
					put("mount3", "4_287");
					put("kmount", "8_40");      // Knight's Mount: [gondor] mount on a knight; its text answers a skirmish won only
					put("sword", "4_291");      // Sword of Rohan: [rohan] hand weapon, a possession that is no mount
					put("sword2", "4_291");
					put("blackrider", "12_161"); // Black Rider: [wraith] Nazgul, strength +1 for each companion
					put("steed", "1_208");      // Black Steed: [wraith] mount on a Nazgul; strength +2 at a plains site
					put("beauty", "1_205");     // Beauty Is Fading: [wraith] Maneuver event, exert a Nazgul to discard a Free Peoples possession or condition
					put("mark", "12_114");      // For the Mark: [rohan] event, spot a [rohan] Man to discard a possession from play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HalethStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Haleth, Son of Háma
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: While you can spot 2 mounts, Haleth is strength +2.
		 * 	While you can spot 3 mounts, Haleth is <b>damage +1</b>.
		 * 	While you can spot 4 Free Peoples mounts, those mounts can not be discarded by a Shadow player.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("haleth");

		assertEquals("Haleth", card.getBlueprint().getTitle());
		assertEquals("Son of Háma", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void WithTwoMountsOfTwoCulturesHalethIsStrengthPlus2AndNotYetDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haleth = scn.GetFreepsCard("haleth");
		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var knight = scn.GetFreepsCard("knight");
		var mount1 = scn.GetFreepsCard("mount1");
		var sword = scn.GetFreepsCard("sword");
		var kmount = scn.GetFreepsCard("kmount");
		scn.MoveCompanionsToTable(haleth, rider, rider2, knight);
		scn.AttachCardsTo(rider, mount1);
		scn.AttachCardsTo(rider2, sword);
		scn.MoveCardsToHand(kmount);

		scn.StartGame();

		// One mount, beside a possession that is no mount.
		assertAttachedTo(mount1, rider);
		assertAttachedTo(sword, rider2);
		assertEquals(5, scn.GetStrength(haleth));
		assertEquals(5, scn.GetStrength(rider));

		scn.FreepsPlayCard(kmount);
		assertAttachedTo(kmount, knight);
		// A [rohan] mount and a [gondor] one: 5 + 2. Two mounts do not make him damage +1. The Rider is untouched.
		assertEquals(7, scn.GetStrength(haleth));
		assertEquals(0, scn.GetKeywordCount(haleth, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(rider));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShadowMountIsSpottedTooAndThreeMountsMakeHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haleth = scn.GetFreepsCard("haleth");
		var rider = scn.GetFreepsCard("rider");
		var knight = scn.GetFreepsCard("knight");
		var mount1 = scn.GetFreepsCard("mount1");
		var kmount = scn.GetFreepsCard("kmount");
		var blackrider = scn.GetShadowCard("blackrider");
		var steed = scn.GetShadowCard("steed");
		scn.MoveCompanionsToTable(haleth, rider, knight);
		scn.AttachCardsTo(rider, mount1);
		scn.AttachCardsTo(knight, kmount);

		scn.StartGame();

		assertAttachedTo(mount1, rider);
		assertAttachedTo(kmount, knight);
		assertEquals(7, scn.GetStrength(haleth));
		assertEquals(0, scn.GetKeywordCount(haleth, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.DAMAGE));

		// A minion and its mount cheated onto the table in the fellowship phase, to read his standing text.
		scn.MoveMinionsToTable(blackrider);
		scn.AttachCardsTo(blackrider, steed);
		// The Black Steed is the third mount: damage +1, and the +2 stays. The Rider is untouched.
		assertEquals(1, scn.GetKeywordCount(haleth, Keyword.DAMAGE));
		assertEquals(7, scn.GetStrength(haleth));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.DAMAGE));
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithFourFreePeoplesMountsAShadowPlayerCannotDiscardThemAndTheFreePeoplesPlayerCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haleth = scn.GetFreepsCard("haleth");
		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var knight = scn.GetFreepsCard("knight");
		var mount1 = scn.GetFreepsCard("mount1");
		var mount2 = scn.GetFreepsCard("mount2");
		var mount3 = scn.GetFreepsCard("mount3");
		var kmount = scn.GetFreepsCard("kmount");
		var sword = scn.GetFreepsCard("sword");
		var sword2 = scn.GetFreepsCard("sword2");
		var mark = scn.GetFreepsCard("mark");
		var blackrider = scn.GetShadowCard("blackrider");
		var beauty = scn.GetShadowCard("beauty");
		scn.MoveCompanionsToTable(haleth, rider, rider2, knight);
		scn.AttachCardsTo(haleth, mount1);
		scn.AttachCardsTo(rider, mount2);
		scn.AttachCardsTo(rider2, mount3);
		scn.AttachCardsTo(rider2, sword);
		scn.AttachCardsTo(rider, sword2);
		scn.AttachCardsTo(knight, kmount);
		scn.MoveCardsToHand(mark);
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		assertAttachedTo(mount1, haleth);
		assertAttachedTo(mount2, rider);
		assertAttachedTo(mount3, rider2);
		assertAttachedTo(kmount, knight);
		assertAttachedTo(sword, rider2);
		assertAttachedTo(sword2, rider);
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(blackrider);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(beauty));
		scn.ShadowPlayCard(beauty);
		// Four Free Peoples mounts, of [rohan] and [gondor]: none of them is offered to the Shadow player; the swords are.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, sword2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(mount1, mount2, mount3, kmount));
		scn.ShadowChooseCard(sword);
		assertInDiscard(sword);
		assertInPlay(mount1, mount2, mount3, kmount);

		// The Free Peoples player is no Shadow player: he may discard his own mount.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPlayCard(mark);
		// Played in the maneuver phase, it exerts a [rohan] Man who has resistance 3 or more first.
		assertEquals(0, scn.GetWoundsOn(rider));
		scn.FreepsChooseCard(rider);
		assertEquals(1, scn.GetWoundsOn(rider));
		assertTrue(scn.FreepsHasCardChoiceAvailable(mount1, mount2, mount3, kmount));
		scn.FreepsChooseCard(mount3);
		assertInDiscard(mount3);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithThreeFreePeoplesMountsAndAShadowMountAShadowPlayerMayDiscardThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haleth = scn.GetFreepsCard("haleth");
		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var knight = scn.GetFreepsCard("knight");
		var mount1 = scn.GetFreepsCard("mount1");
		var mount2 = scn.GetFreepsCard("mount2");
		var kmount = scn.GetFreepsCard("kmount");
		var sword = scn.GetFreepsCard("sword");
		var blackrider = scn.GetShadowCard("blackrider");
		var steed = scn.GetShadowCard("steed");
		var beauty = scn.GetShadowCard("beauty");
		scn.MoveCompanionsToTable(haleth, rider, rider2, knight);
		scn.AttachCardsTo(haleth, mount1);
		scn.AttachCardsTo(rider, mount2);
		scn.AttachCardsTo(rider2, sword);
		scn.AttachCardsTo(knight, kmount);
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		assertAttachedTo(mount1, haleth);
		assertAttachedTo(mount2, rider);
		assertAttachedTo(kmount, knight);
		assertAttachedTo(sword, rider2);
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(blackrider);
		scn.AttachCardsTo(blackrider, steed);
		assertAttachedTo(steed, blackrider);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPass();
		scn.ShadowPlayCard(beauty);
		// Four mounts, three of them Free Peoples: the Free Peoples mounts are offered beside the sword.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mount1, mount2, kmount, sword));
		scn.ShadowChooseCard(mount2);
		assertInDiscard(mount2);
		assertInPlay(mount1, kmount, sword);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
