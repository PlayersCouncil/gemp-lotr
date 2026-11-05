package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_004_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The skirmishes of
	// turn 1 are at The Troll Hoard (site 2); the minions are laid in the Shadow phase.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spear", "32_4");
					put("spear2", "32_4");

					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion, strength 7; a maneuver ability not used
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, strength 7 (+1 for each [dwarven] follower he bears: none)
					put("dain", "32_2");        // Dain Ironfoot, Lord of the Iron Hills: [dwarven] ally, strength 7; his regroup trigger not reached
					put("dori", "30_8");        // Dori, Really a decent fellow: a [dwarven] follower, no character
					// Crown of Erebor: its Assignment ability, with no Thorin in play, lets [dwarven] allies be assigned
					put("crown", "32_1");

					put("azog", "32_28");       // Azog, Commander of the Wolves Army: [moria] Orc, strength 9; a maneuver ability at a battleground
					put("goblin", "32_27");     // Assaulting Goblin: [moria] Orc, strength 7; a response at a battleground
					put("swarg", "31_25");      // Savage Warg: a mount (strength +2, fierce); its win trigger exerts a [dwarven] companion
					put("vwarg", "32_32");      // Vicious Warg: a mount (strength +3, fierce); its play trigger is never met (cheated on)
					put("greatgoblin", "31_36");// The Great Goblin: unique [moria] Orc, strength 10, fierce; a maneuver ability not used
					put("sceptre", "33_43");    // Great Goblin's Sceptre: a hand weapon, no mount; on The Great Goblin each other Orc is +2
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DwarvenSpearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Dwarven Spear
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [dwarven] character.
		 * 	While skirmishing a mounted minion, bearer is strength +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spear");

		assertEquals("Dwarven Spear", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnADwarvenCompanionOrAllyAndOnNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var nori = scn.GetFreepsCard("nori");
		var dain = scn.GetFreepsCard("dain");
		var dori = scn.GetFreepsCard("dori");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(nori);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(dori);

		scn.StartGame();
		scn.MoveCardsToHand(spear);
		scn.FreepsRefreshDecision();
		assertInHand(spear);
		assertInZone(Zone.SUPPORT, dain, dori);

		assertTrue(scn.FreepsPlayAvailable(spear));
		scn.FreepsPlayCard(spear);
		// A [dwarven] companion and a [dwarven] ally; not Bilbo, a [shire] companion, nor a [dwarven] follower.
		assertTrue(scn.FreepsHasCardChoiceAvailable(nori, dain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, dori));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(dain);

		assertAttachedTo(spear, dain);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlusOneOnlyOnceHisSkirmishWithAMountedMinionHasBegun() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var nori = scn.GetFreepsCard("nori");
		var thorin = scn.GetFreepsCard("thorin");
		var azog = scn.GetShadowCard("azog");
		var goblin = scn.GetShadowCard("goblin");
		var swarg = scn.GetShadowCard("swarg");
		var vwarg = scn.GetShadowCard("vwarg");
		scn.MoveCompanionsToTable(nori, thorin);

		scn.StartGame();
		scn.AttachCardsTo(nori, spear);
		assertAttachedTo(spear, nori);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog, goblin);
		scn.AttachCardsTo(azog, swarg);
		scn.AttachCardsTo(goblin, vwarg);
		assertAttachedTo(swarg, azog);
		assertAttachedTo(vwarg, goblin);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{nori, azog}, new PhysicalCardImpl[]{thorin, goblin});
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Thorin, who does not bear the Spear, skirmishes the mounted Goblin first; Nori is assigned to a mounted
		// minion and waits.
		scn.FreepsResolveSkirmish(thorin);
		assertTrue(scn.IsCharSkirmishingAgainst(thorin, goblin));
		assertEquals(7, scn.GetStrength(thorin));
		// 7 + 2 for the Spear, and not yet the +1.
		assertEquals(9, scn.GetStrength(nori));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(nori);
		assertTrue(scn.IsCharSkirmishingAgainst(nori, azog));
		// 7 + 2 for the Spear + 1 skirmishing a mounted minion.
		assertEquals(10, scn.GetStrength(nori));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllyBearerIsStrengthPlusOneSkirmishingAMountedMinionAndNoBearerIsAgainstAMinionWithAnItemThatIsNoMount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var spear2 = scn.GetFreepsCard("spear2");
		var nori = scn.GetFreepsCard("nori");
		var dain = scn.GetFreepsCard("dain");
		var crown = scn.GetFreepsCard("crown");
		var azog = scn.GetShadowCard("azog");
		var swarg = scn.GetShadowCard("swarg");
		var greatgoblin = scn.GetShadowCard("greatgoblin");
		var sceptre = scn.GetShadowCard("sceptre");
		scn.MoveCompanionsToTable(nori);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		scn.AttachCardsTo(nori, spear);
		scn.AttachCardsTo(dain, spear2);
		assertAttachedTo(spear, nori);
		assertAttachedTo(spear2, dain);
		assertInZone(Zone.SUPPORT, crown);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog, greatgoblin);
		scn.AttachCardsTo(azog, swarg);
		scn.AttachCardsTo(greatgoblin, sceptre);
		assertAttachedTo(swarg, azog);
		assertAttachedTo(sceptre, greatgoblin);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsUseCardAction(crown);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{nori, greatgoblin}, new PhysicalCardImpl[]{dain, azog});
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The Great Goblin bears a hand weapon, and no mount.
		scn.FreepsResolveSkirmish(nori);
		assertTrue(scn.IsCharSkirmishingAgainst(nori, greatgoblin));
		assertEquals(9, scn.GetStrength(nori));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(dain);
		assertTrue(scn.IsCharSkirmishingAgainst(dain, azog));
		// 7 + 2 for the Spear + 1 skirmishing a mounted minion.
		assertEquals(10, scn.GetStrength(dain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
